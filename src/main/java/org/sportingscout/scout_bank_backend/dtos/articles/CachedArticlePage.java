package org.sportingscout.scout_bank_backend.dtos.articles;

import java.io.Serializable;
import java.util.List;

public record CachedArticlePage(
    List<ArticleWithMedia> content,
    long totalElements,
    int pageNumber,
    int pageSize) implements Serializable {
}
