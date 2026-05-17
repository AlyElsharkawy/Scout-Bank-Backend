package org.sportingscout.scout_bank_backend.services.users;

import org.sportingscout.scout_bank_backend.repositories.UserRanksRepository;
import org.sportingscout.scout_bank_backend.entities.UserRank;
import org.sportingscout.scout_bank_backend.mappers.ranks.CreateUserRankRequestMapper;
import org.sportingscout.scout_bank_backend.dtos.ranks.CreateUserRankRequest;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserRanksService {
  private static final Logger logger = LoggerFactory.getLogger(UserRanksService.class);
  private final UserRanksRepository ranksRepo;
  private final CreateUserRankRequestMapper createRequestMapper;

  public UserRanksService(UserRanksRepository ranksRepo,
      CreateUserRankRequestMapper createRequestMapper) {
    this.ranksRepo = ranksRepo;
    this.createRequestMapper = createRequestMapper;
  }

  public List<UserRank> getAllRanks() {
    try {
      logger.debug("Attempting to fetch all UserRank instances");
      List<UserRank> temp = ranksRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all UserRank instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all UserRank fetch: ", e);
      throw e;
    }
  }

  public void createRank(CreateUserRankRequest request) {
    try {
      UserRank rank = createRequestMapper.toEntity(request);
      ranksRepo.save(rank);
    } catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create UserRank with duplicate unique name or null required field: ", e.getMessage());
      throw new IllegalArgumentException("A UserRank with this name already exists or required field is null.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during UserRank creation: ", e);
      throw e;
    }
  }

  public void editRank(Long id, CreateUserRankRequest request) {
    UserRank existingRank = ranksRepo.findById(id)
        .orElseThrow(() -> new NoSuchElementException("UserRank not found with id: " + id));
    createRequestMapper.updateRankFromDto(request, existingRank);
    ranksRepo.save(existingRank);
  }
}
