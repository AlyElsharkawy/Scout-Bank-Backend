package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;

import org.springframework.stereotype.Service;

@Service
public class ArticleVersionsService {
  private final ArticleVersionRepository articleVersionRepo;

  public ArticleVersionsService(ArticleVersionRepository articleVersionRepo) {
    this.articleVersionRepo = articleVersionRepo;
  }

  // public Long createArticleVersion() {
  // }

  public void approveArticleVersion(Long articleId, ApprovalStatus status) {
  }
}
