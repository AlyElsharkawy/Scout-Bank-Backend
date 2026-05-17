package org.sportingscout.scout_bank_backend.mappers.users;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;
import org.mapstruct.AfterMapping;

import org.sportingscout.scout_bank_backend.dtos.users.CreateUserRequest;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Collection;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CreateUserRequestMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(source = "organizationId", target = "organization.id")

  @Mapping(source = "roleId", target = "role.id")
  @Mapping(source = "rankId", target = "rank.id")

  ApplicationUser toEntity(CreateUserRequest request);
}
