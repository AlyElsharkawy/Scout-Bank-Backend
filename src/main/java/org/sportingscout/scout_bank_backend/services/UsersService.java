package org.sportingscout.scout_bank_backend.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.Organization;
import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.sportingscout.scout_bank_backend.repositories.OrganizationsRepository;
import org.sportingscout.scout_bank_backend.dtos.users.CreateUserRequest;
import org.sportingscout.scout_bank_backend.dtos.users.UpdateUserRequest;
import org.sportingscout.scout_bank_backend.mappers.CreateUserRequestMapper;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsersService {
  private final UsersRepository usersRepo;
  private final OrganizationsRepository organizationsRepo;
  private final CreateUserRequestMapper requestMapper;
  private final PasswordEncoder passwordEncoder;

  public UsersService(UsersRepository usersRepo, OrganizationsRepository organizationsRepo,
      CreateUserRequestMapper requestMapper, PasswordEncoder passwordEncoder) {
    this.usersRepo = usersRepo;
    this.requestMapper = requestMapper;
    this.organizationsRepo = organizationsRepo;
    this.passwordEncoder = passwordEncoder;
  }

  private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

  // DTOs will be added later!
  // Thank you for notifying me!
  // I am currently prioritizing basic functionality over polish
  // Of course, we will have a DTO in production!
  public Optional<ApplicationUser> getUserById(Long id) {
    try {
      logger.debug("Attempting to fetch ApplicationUser with id: {}", id);
      Optional<ApplicationUser> temp = usersRepo.findById(id);
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching user with id{}: ", id, e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during user fetch with id: {}", id, e);
      throw e;
    }
  }

  public List<ApplicationUser> getAllUsers() {
    try {
      logger.debug("Attempting to fetch all ApplicationUser instances");
      List<ApplicationUser> temp = usersRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ApplicationUser instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all users fetch: ", e);
      throw e;
    }
  }

  public void createUser(CreateUserRequest request) {
    try {
      ApplicationUser entity = requestMapper.toEntity(request);
      entity.setPassword(passwordEncoder.encode(entity.getPassword()));
      usersRepo.save(entity);
      logger.debug("Successfully created user | ID: {}, Email: {}", entity.getId(), request.email());
    } catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create user with duplicate unique field: ", e.getMessage());
      throw new IllegalArgumentException("A user with this email or phone number already exists.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during user creation: ", e);
      throw e;
    }
  }

  public void deleteUser(Long id) {
    try {
      logger.debug("Attempting to delete user with ID: {}", id);
      if (!usersRepo.existsById(id)) {
        logger.warn("Delete failed: User with ID {} does not exist", id);
        throw new NoSuchElementException("Cannot delete: User not found.");
      }

      usersRepo.deleteById(id);
      logger.info("Successfully deleted user with ID: {}", id);

    } catch (DataAccessException e) {
      logger.error("Database error during deletion of user with id: {}", id, e.getMessage());
      throw new RuntimeException("Could not reach PostgreSQL during deletion.");
    } catch (Exception e) {
      logger.error("Unexpected error during user deletion: ", e);
      throw e;
    }
  }

  public void updateUser(Long id, UpdateUserRequest req) {
    try {
      ApplicationUser existingUser = usersRepo.findById(id)
          .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
      Organization existingOrganization = organizationsRepo.findById(req.organizationId())
          .orElseThrow(() -> new NoSuchElementException("Organization not found with id: " + req.organizationId()));
      existingUser.setName(req.name());
      existingUser.setEmail(req.email());
      existingUser.setPhoneNumber(req.phoneNumber());
      existingUser.setProfilePicture(req.profilePicture());
      existingUser.setOrganization(existingOrganization);

      usersRepo.save(existingUser);
    } catch (DataIntegrityViolationException e) {
      logger.error("Update failed - unique constraint violation for user {}: {}", id, e.getMessage());
      throw new IllegalArgumentException(
          "Email or Phone Number already in use by another user or organization ID does not exist.");
    }
  }
}
