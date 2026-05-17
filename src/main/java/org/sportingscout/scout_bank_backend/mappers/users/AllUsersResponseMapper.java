package org.sportingscout.scout_bank_backend.mappers.users;

import org.sportingscout.scout_bank_backend.dtos.users.AllUsersResponse;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import org.springframework.security.core.GrantedAuthority;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Collection;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AllUsersResponseMapper {

  @Mapping(source = "organization.name", target = "organizationName")
  AllUsersResponse toResponse(ApplicationUser user);

  List<AllUsersResponse> toResponse(List<ApplicationUser> users);

  default List<String> map(Collection<? extends GrantedAuthority> authorities) {
    if (authorities == null) {
      return null;
    }
    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
  }
}
