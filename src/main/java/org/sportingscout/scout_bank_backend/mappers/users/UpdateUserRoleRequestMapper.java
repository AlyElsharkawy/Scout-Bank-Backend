package org.sportingscout.scout_bank_backend.mappers.users;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.sportingscout.scout_bank_backend.dtos.roles.UpdateUserRoleRequest;
import org.sportingscout.scout_bank_backend.entities.UserRole;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UpdateUserRoleRequestMapper {

  @Mapping(target = "id", ignore = true)
  void updateRoleFromDto(UpdateUserRoleRequest dto, @MappingTarget UserRole role);
}
