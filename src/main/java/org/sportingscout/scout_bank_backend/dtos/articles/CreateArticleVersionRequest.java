package org.sportingscout.scout_bank_backend.dtos.articles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.util.List;

public record CreateArticleVersionRequest(
    @NotBlank(message = "Title cannot be blank") String title,
    @NotNull(message = "Article Type is required") Long articleTypeId,
    @NotBlank(message = "Article Version content cannot be empty") @Size(max = 65535, message = "Cannot contain more than 65,535 characters") String content,
    @NotEmpty(message = "Article Version must contain tags") List<Long> tagIds,
    @NotBlank(message = "Update note is required") String updateNote,
    @Valid List<ArticleMediaRequest> media) {

  public record ArticleMediaRequest(
      @Size(max = 511, message = "Caption cannot exceed 511 characters") String caption,
      @NotBlank String key) {
  }
}
