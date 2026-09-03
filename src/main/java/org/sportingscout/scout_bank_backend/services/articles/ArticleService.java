package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.entities.*;
import org.sportingscout.scout_bank_backend.repositories.articles.*;
import org.sportingscout.scout_bank_backend.dtos.articles.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final ArticleVersionRepository articleVersionRepository;
  private final ArticleVersionsService articleVersionsService;
  private final PagedResourcesAssembler<ArticleWithMedia> pagedAssembler;

  public ArticleService(
      ArticleRepository articleRepository,
      ArticleVersionRepository articleVersionRepository,
      ArticleVersionsService articleVersionsService,
      PagedResourcesAssembler<ArticleWithMedia> pagedAssembler) {
    this.articleRepository = articleRepository;
    this.articleVersionRepository = articleVersionRepository;
    this.articleVersionsService = articleVersionsService;
    this.pagedAssembler = pagedAssembler;
  }

  @Cacheable(value = "articles", key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort.toString()")
  public CachedArticlePage getAllArticles(Pageable pageable) {
    Page<Article> allArticles = this.articleRepository.findAll(pageable);

    List<ArticleVersion> articleVersions = allArticles.getContent().stream()
        .map(Article::getLiveArticle)
        .toList();

    List<ArticleVersionWithMedia> articleVersionWithMedias = this.articleVersionsService
        .assignMediaToArticleSubversions(articleVersions);

    List<ArticleWithMedia> finalArticles = new ArrayList<>(articleVersionWithMedias.size());
    for (int i = 0; i < articleVersionWithMedias.size(); i++) {
      finalArticles.add(new ArticleWithMedia(
          allArticles.getContent().get(i), articleVersionWithMedias.get(i)));
    }

    return new CachedArticlePage(
        finalArticles,
        allArticles.getTotalElements(),
        pageable.getPageNumber(),
        pageable.getPageSize());
  }

  @Transactional
  @CacheEvict(value = "articles", allEntries = true) // Evict cache on changes
  public Long createArticle(UUID externalId, Integer majorVersion, Integer minorVersion) {
    ArticleVersion articleVersion = this.articleVersionRepository
        .findByExternalIdAndMajorVersionAndMinorVersion(externalId, majorVersion, minorVersion)
        .orElseThrow(() -> new NoSuchElementException(String.format(
            "ArticleVersion with externalId %s, majorVersion %d, and minorVersion %d does not exist", externalId,
            majorVersion, minorVersion)));

    if (articleVersion.getStatus() != ApprovalStatus.APPROVED) {
      throw new IllegalArgumentException("Article has not been approved");
    }

    Article article = new Article(articleVersion);
    return this.articleRepository.save(article).getId();
  }

  @Transactional
  @CacheEvict(value = "articles", allEntries = true)
  public void deleteArticle(Long id) {
    Optional<Article> article = this.articleRepository.findById(id);
    if (article.isEmpty()) {
      throw new NoSuchElementException(
          String.format("Article with id %d does not exist", id));
    }
    this.articleRepository.delete(article.get());
  }
}
