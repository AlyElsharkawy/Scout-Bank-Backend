package org.sportingscout.scout_bank_backend.dtos.users;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public record SingleUserResponse(
    String email,
    String name,
    String profilePicture,
    String phoneNumber,
    Long organizationId,
    String organizationName,
    Long rankId,
    String rankName,
    Long roleId,
    String roleName,
    // List<String> authorities,
    LocalDateTime createdAt,
    LocalDate birthDate,
    List<String> authorities) {
}
