package org.sportingscout.scout_bank_backend.dtos.articles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateArticleVersionRequest(
    @NotBlank(message = "Title cannot be blank") String title,
    Long articleTypeId,
    @NotBlank(message = "Article Version cannot be empty") @Size(max = 65535, message = "Cannot contain more than 65,535 characters") String content,
    List<UUID> editorIds,
    @NotEmpty(message = "Article Version must contain tags") List<Long> tagIds,
    @NotBlank(message = "Update note is required") String updateNote) {
}
