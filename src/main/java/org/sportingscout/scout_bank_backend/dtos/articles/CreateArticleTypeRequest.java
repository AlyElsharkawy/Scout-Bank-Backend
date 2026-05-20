package org.sportingscout.scout_bank_backend.dtos.articles;

import jakarta.validation.constraints.NotBlank;

public record CreateArticleTypeRequest(
    @NotBlank(message = "Name cannot be completely empty or whitespace") String name,
    @NotBlank(message = "Description cannot be completely empty or whitespace") String description) {
}
