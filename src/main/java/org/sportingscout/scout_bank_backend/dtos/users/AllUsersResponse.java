package org.sportingscout.scout_bank_backend.dtos.users;

import java.util.List;

public record AllUsersResponse(
    String name,
    String email,
    String phoneNumber,
    String organizationName,
    List<String> authorities) {
}
