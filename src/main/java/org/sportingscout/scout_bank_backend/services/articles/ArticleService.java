package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.entities.Article;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;
import org.sportingscout.scout_bank_backend.services.articles.ArticleVersionsService;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleWithMedia;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleVersionWithMedia;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final ArticleVersionRepository articleVersionRepository;
  private final ArticleVersionsService articleVersionsService;

  ArticleService(
      ArticleRepository articleRepository,
      ArticleVersionRepository articleVersionRepository,
      ArticleVersionsService articleVersionsService) {
    this.articleRepository = articleRepository;
    this.articleVersionRepository = articleVersionRepository;
    this.articleVersionsService = articleVersionsService;
  }

  public Page<ArticleWithMedia> getAllArticles(Pageable pageable) {
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

    return new PageImpl<>(finalArticles, pageable, allArticles.getTotalElements());
  }

  @Transactional
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
  public void deleteArticle(Long id) {
    Optional<Article> article = this.articleRepository.findById(id);
    if (article.isEmpty()) {
      throw new NoSuchElementException(
          String.format("Article with id %d does not exist", id));
    }
    this.articleRepository.delete(article.get());
  }
}
