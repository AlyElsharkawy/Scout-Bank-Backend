package org.sportingscout.scout_bank_backend.services.users;

import org.sportingscout.scout_bank_backend.entities.UserRole;
import org.sportingscout.scout_bank_backend.repositories.UserRolesRepository;
import org.sportingscout.scout_bank_backend.dtos.roles.CreateUserRoleRequest;
import org.sportingscout.scout_bank_backend.dtos.roles.UpdateUserRoleRequest;
import org.sportingscout.scout_bank_backend.mappers.users.CreateUserRoleRequestMapper;
import org.sportingscout.scout_bank_backend.mappers.users.UpdateUserRoleRequestMapper;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserRolesService {
  private static final Logger logger = LoggerFactory.getLogger(UserRolesService.class);
  private final UserRolesRepository rolesRepo;
  private final CreateUserRoleRequestMapper createRequestMapper;
  private final UpdateUserRoleRequestMapper updateRequestMapper;

  public UserRolesService(UserRolesRepository rolesRepo,
      CreateUserRoleRequestMapper createUserRoleRequestMapper,
      UpdateUserRoleRequestMapper updateUserRoleRequestMapper) {
    this.rolesRepo = rolesRepo;
    this.createRequestMapper = createUserRoleRequestMapper;
    this.updateRequestMapper = updateUserRoleRequestMapper;
  }

  public List<UserRole> getAllRoles() {
    try {
      logger.debug("Attempting to fetch all UserRole instances");
      List<UserRole> temp = rolesRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all UserRole instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all users fetch: ", e);
      throw e;
    }
  }

  public Optional<UserRole> getRole(Long id) {
    try {
      logger.debug("Attempting to fetch UserRole with id: {}", id);
      Optional<UserRole> temp = rolesRepo.findById(id);
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching UserRole with id{}: ", id, e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during UserRole fetch with id: {}", id, e);
      throw e;
    }
  }

  public void createRole(CreateUserRoleRequest request) {
    try {
      UserRole temp = createRequestMapper.toEntity(request);
      rolesRepo.save(temp);
    }

    catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create UserRole with duplicate unique field or null required field: ", e.getMessage());
      throw new IllegalArgumentException("A UserRole with this name already exists or a required field is null.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during UserRole creation: ", e);
      throw e;
    }
  }

  @Transactional
  public void updateRole(Long id, UpdateUserRoleRequest request) {
    UserRole existingRole = rolesRepo.findById(id)
        .orElseThrow(() -> new NoSuchElementException("UserRole not found with id: " + id));
    updateRequestMapper.updateRoleFromDto(request, existingRole);
    rolesRepo.save(existingRole);
  }
}
