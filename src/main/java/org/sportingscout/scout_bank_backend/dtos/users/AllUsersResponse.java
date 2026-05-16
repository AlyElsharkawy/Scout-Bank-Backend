package org.sportingscout.scout_bank_backend.dtos.users;

import java.util.List;

public record AllUsersResponse(
    String email,
    String name,
    String phoneNumber,
    String organizationName,
    List<String> authorities) {
}
