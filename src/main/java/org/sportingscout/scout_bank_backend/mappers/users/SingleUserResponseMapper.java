package org.sportingscout.scout_bank_backend.mappers.users;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

import org.sportingscout.scout_bank_backend.dtos.users.SingleUserResponse;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SingleUserResponseMapper {

  @Mapping(source = "organization.id", target = "organizationId")
  @Mapping(source = "organization.name", target = "organizationName")
  @Mapping(source = "rank.id", target = "rankId")
  @Mapping(source = "rank.name", target = "rankName")
  @Mapping(source = "role.id", target = "roleId")
  @Mapping(source = "role.name", target = "roleName")
  // record
  SingleUserResponse toResponse(ApplicationUser user);

  default LocalDateTime mapInstant(Instant instant) {
    if (instant == null) {
      return null;
    }
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
  }

  default List<String> map(Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null) {
      return null;
    }
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
  }
}
