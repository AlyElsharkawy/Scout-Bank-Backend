package org.sportingscout.scout_bank_backend.dtos.roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UpdateUserRoleRequest(
    @NotBlank(message = "Name cannot be empty or completely whitespace") String name,

    @NotBlank(message = "Description cannot be empty or completely whitespace") String description,

    @NotNull(message = "Authorities set cannot be null") Set<String> authorities) {
}
