package org.sportingscout.scout_bank_backend.dtos.articles;

import org.springframework.data.domain.Page;
import org.sportingscout.scout_bank_backend.entities.Article;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public record CachedArticleSummaryPage(
    List<Summary> content,
    long totalElements,
    int pageNumber,
    int pageSize) implements Serializable {

  public static CachedArticleSummaryPage from(Page<Article> page, Function<String, String> urlResolver) {
    List<Summary> summaries = page.getContent().stream()
        .map(article -> Summary.fromArticle(article, urlResolver))
        .toList();

    return new CachedArticleSummaryPage(
        summaries,
        page.getTotalElements(),
        page.getNumber(),
        page.getSize());
  }

  public record Summary(
      String title,
      String summary,
      String thumbnail,
      String authorName,
      String externalId,
      Integer majorVersion,
      Integer minorVersion) implements Serializable {

    public static Summary fromArticle(Article article, Function<String, String> urlResolver) {
      if (article == null || article.getLiveArticle() == null) {
        return new Summary(null, null, null, null, null, null, null);
      }
      ArticleVersion live = article.getLiveArticle();

      String rawKey = live.getThumbnail();
      String presignedUrl = (rawKey != null && !rawKey.isBlank())
          ? urlResolver.apply(rawKey)
          : null;

      return new Summary(
          live.getTitle(), live.getSummary(), presignedUrl, live.getAuthor().getName(),
          live.getExternalId().toString(), live.getMajorVersion(), live.getMinorVersion());
    }
  }
}
