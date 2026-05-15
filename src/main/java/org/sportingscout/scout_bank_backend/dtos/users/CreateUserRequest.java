package org.sportingscout.scout_bank_backend.dtos.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserRequest(
    @NotBlank(message = "Name cannot be empty or completely whitespace") String name,

    String profilePicture,

    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

    @Pattern(regexp = "^01[0125]\\d{8}$", message = "Invalid Egyptian mobile number format") @NotBlank(message = "Phone number cannot be empty or completely whitespace") String phoneNumber,

    @NotBlank(message = "Password cannot be empty or completely whitespace") @Size(min = 8, message = "Password must at least be 8 characters long") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one digit, lowercase character, and uppercase character") String password,

    List<String> authorities,

    Long organizationId) {
}
