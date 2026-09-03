package org.sportingscout.scout_bank_backend.dtos.articles;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.List;

//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record CachedArticlePage(
    List<ArticleWithMedia> content,
    long totalElements,
    int pageNumber,
    int pageSize) implements Serializable {
}
