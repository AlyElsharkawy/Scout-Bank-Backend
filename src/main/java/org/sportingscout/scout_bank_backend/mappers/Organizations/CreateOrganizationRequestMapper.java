package org.sportingscout.scout_bank_backend.mappers.Organizations;

import org.sportingscout.scout_bank_backend.entities.Organization;
import org.sportingscout.scout_bank_backend.dtos.organizations.CreateOrganizationRequest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CreateOrganizationRequestMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)

  Organization toEntity(CreateOrganizationRequest request);
}
