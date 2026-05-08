package org.sportingscout.scout_bank_backend.dtos.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
    @NotBlank(message = "Name cannot be blank or completely whitespace") String name,
    @Email(message = "A valid email is required") String email,
    @Pattern(regexp = "^01[0125]\\d{8}$", message = "Invalid Egyptian mobile number format") @NotBlank(message = "Phone number cannot be empty or completely whitespace") String phoneNumber,
    String profilePicture,
    Long organizationId) {
}
