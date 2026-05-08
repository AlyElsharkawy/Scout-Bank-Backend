package org.sportingscout.scout_bank_backend.dtos.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;

public record CreateUserRequest(
    @NotBlank(message = "Name cannot be empty or completely whitespace") String name,
    String profilePicture,
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
    @Pattern(regexp = "^01[0125]\\d{8}$", message = "Invalid Egyptian mobile number format") @NotBlank(message = "Phone number cannot be empty or completely whitespace") String phoneNumber,
    Long organizationId) {
}
