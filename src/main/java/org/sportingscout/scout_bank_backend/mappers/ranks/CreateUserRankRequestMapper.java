package org.sportingscout.scout_bank_backend.mappers.ranks;

import org.sportingscout.scout_bank_backend.entities.UserRank;
import org.sportingscout.scout_bank_backend.dtos.ranks.CreateUserRankRequest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Builder;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface CreateUserRankRequestMapper {
  @Mapping(target = "id", ignore = true)
  UserRank toEntity(CreateUserRankRequest request);

  @Mapping(target = "id", ignore = true)
  void updateRankFromDto(CreateUserRankRequest request, @MappingTarget UserRank rank);
}
