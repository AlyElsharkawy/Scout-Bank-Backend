package org.sportingscout.scout_bank_backend.dtos.roles;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CreateUserRoleRequest(
    @NotBlank(message = "Name cannot be empty or completely whitespace") String name,

    @NotBlank(message = "Description cannot be empty or completely whitespace") String description,

    Set<String> authorities) {
}
