package org.sportingscout.scout_bank_backend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

import org.sportingscout.scout_bank_backend.dtos.users.CreateUserRequest;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CreateUserRequestMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(source = "organizationId", target = "organization.id")

  ApplicationUser toEntity(CreateUserRequest request);
}
