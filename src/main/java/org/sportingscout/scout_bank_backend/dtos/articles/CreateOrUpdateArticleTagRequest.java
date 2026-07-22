package org.sportingscout.scout_bank_backend.dtos.articles;

import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateArticleTagRequest(
    @NotBlank(message = "Tag name cannot be empty") String name) {
}
