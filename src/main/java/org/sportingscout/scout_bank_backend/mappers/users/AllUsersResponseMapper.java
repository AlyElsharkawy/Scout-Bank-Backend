package org.sportingscout.scout_bank_backend.mappers.users;

import org.sportingscout.scout_bank_backend.dtos.users.AllUsersResponseDTO;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AllUsersResponseMapper {

  @Mapping(source = "organization.name", target = "organizationName")
  AllUsersResponseDTO toResponse(ApplicationUser user);

  List<AllUsersResponseDTO> toResponse(List<ApplicationUser> users);

}
