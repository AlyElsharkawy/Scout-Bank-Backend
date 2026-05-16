package org.sportingscout.scout_bank_backend.mappers.users;

import org.sportingscout.scout_bank_backend.dtos.users.AllUsersResponse;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AllUsersResponseMapper {

  @Mapping(source = "organization.name", target = "organizationName")
  AllUsersResponse toResponse(ApplicationUser user);

  List<AllUsersResponse> toResponse(List<ApplicationUser> users);

}
