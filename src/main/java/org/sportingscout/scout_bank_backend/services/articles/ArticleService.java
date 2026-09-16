package org.sportingscout.scout_bank_backend.services.articles;

import static org.sportingscout.scout_bank_backend.configuration.RedisNamespaces.*;

import org.sportingscout.scout_bank_backend.entities.Article;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;

import org.sportingscout.scout_bank_backend.repositories.articles.ArticleRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;

import org.sportingscout.scout_bank_backend.dtos.articles.ArticleWithMedia;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleVersionWithMedia;
import org.sportingscout.scout_bank_backend.dtos.articles.CachedArticleSummaryPage;
import org.sportingscout.scout_bank_backend.dtos.articles.CachedArticlePage;

import org.sportingscout.scout_bank_backend.services.S3Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.sportingscout.scout_bank_backend.configuration.RedisNamespaces.ARTICLE_SUMMARIES;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final ArticleVersionRepository articleVersionRepository;
  private final ArticleVersionsService articleVersionsService;
  private final S3Service s3Service;

  public ArticleService(
      ArticleRepository articleRepository,
      ArticleVersionRepository articleVersionRepository,
      ArticleVersionsService articleVersionsService,
      S3Service s3Service) {
    this.articleRepository = articleRepository;
    this.articleVersionRepository = articleVersionRepository;
    this.articleVersionsService = articleVersionsService;
    this.s3Service = s3Service;
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

  @Cacheable(value = ARTICLE_SUMMARIES, key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize + ':sort:' + #pageable.sort.toString() + 'term:' + #searchTerm")
  public CachedArticleSummaryPage getAllArticleSummaries(Pageable pageable, String searchTerm) {
    Page<Article> allArticles = this.articleRepository
        .findByLiveArticleTitleContainingIgnoreCase(searchTerm, pageable);

    return CachedArticleSummaryPage.from(allArticles, this.s3Service::getPresignedUrl);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = ARTICLES, allEntries = true),
      @CacheEvict(value = ARTICLE_SUMMARIES, allEntries = true)
  })
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
  @Caching(evict = {
      @CacheEvict(value = ARTICLES, allEntries = true),
      @CacheEvict(value = ARTICLE_SUMMARIES, allEntries = true)
  })
  public void deleteArticle(Long id) {
    Optional<Article> article = this.articleRepository.findById(id);
    if (article.isEmpty()) {
      throw new NoSuchElementException(
          String.format("Article with id %d does not exist", id));
    }
    this.articleRepository.delete(article.get());
  }
}
