package org.sportingscout.scout_bank_backend.mappers.users;

import org.sportingscout.scout_bank_backend.entities.UserRole;
import org.sportingscout.scout_bank_backend.dtos.roles.CreateUserRoleRequest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CreateUserRoleRequestMapper {
  @Mapping(target = "id", ignore = true)
  UserRole toEntity(CreateUserRoleRequest request);
}
