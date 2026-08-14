package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.entities.Article;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class ArticleService {
  private final ArticleRepository articleRepository;
  private final ArticleVersionRepository articleVersionRepository;

  ArticleService(
      ArticleRepository articleRepository,
      ArticleVersionRepository articleVersionRepository) {
    this.articleRepository = articleRepository;
    this.articleVersionRepository = articleVersionRepository;
  }

  public Page<Article> getAllArticles(Pageable pageable) {
    Page<Article> allArticles = this.articleRepository.findAll(pageable);
    return allArticles;
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
}
