package org.sportingscout.scout_bank_backend.dtos.users;

import java.time.LocalDateTime;

public record SingleUserResponse(
    String email,
    String name,
    String profilePicture,
    String phoneNumber,
    String organizationName,
    LocalDateTime createdAt) {
}
