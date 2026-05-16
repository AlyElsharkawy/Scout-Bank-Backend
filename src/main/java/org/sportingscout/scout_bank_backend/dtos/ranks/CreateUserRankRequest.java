package org.sportingscout.scout_bank_backend.dtos.ranks;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRankRequest(
    @NotBlank(message = "Name cannot be empty or completely whitespace") String name,

    @NotBlank(message = "Description cannot be empty or completely whitespace") String description,

    Integer minimumAge, Integer maximumAge) {
}
