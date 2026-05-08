package org.sportingscout.scout_bank_backend.dtos.organizations;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationRequest(
    @NotBlank(message = "Name cannot be empty or completely whitespace") String name,
    @NotBlank(message = "Description cannot be empty or completely whitespace") String description) {
}
