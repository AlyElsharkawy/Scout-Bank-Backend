package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionsRepository;

import org.springframework.stereotype.Service;

@Service
public class ArticleVersionsService {
  private final ArticleVersionsRepository articleVersionsRepo;

  public ArticleVersionsService(ArticleVersionsRepository articleVersionsRepo) {
    this.articleVersionsRepo = articleVersionsRepo;
  }

  // public Long createArticleVersion() {
  // }

  public void approveArticleVersion(Long articleId, ApprovalStatus status) {
  }
}
