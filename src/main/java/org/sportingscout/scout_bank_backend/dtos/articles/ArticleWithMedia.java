package org.sportingscout.scout_bank_backend.dtos.articles;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.Article;

import java.time.Instant;

public record ArticleWithMedia(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    UserSummary createdBy,
    UserSummary updatedBy,
    ArticleVersionWithMedia liveArticle) {

  public ArticleWithMedia(Article article, ArticleVersionWithMedia articleVersionWithMedia) {
    this(
        article.getId(),
        article.getCreatedAt(),
        article.getUpdatedAt(),
        UserSummary.from(article.getCreatedBy()),
        UserSummary.from(article.getUpdatedBy()),
        articleVersionWithMedia);
  }

  public record UserSummary(String name, String organizationName, String rankName, String roleName) {
    public static UserSummary from(ApplicationUser user) {
      if (user == null)
        return null;
      return new UserSummary(
          user.getName(),
          user.getOrganization() != null ? user.getOrganization().getName() : null,
          user.getRank() != null ? user.getRank().getName() : null,
          user.getRole() != null ? user.getRole().getName() : null);
    }
  }
}
