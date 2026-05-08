package org.sportingscout.scout_bank_backend.services;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;

import org.sportingscout.scout_bank_backend.entities.Organization;
import org.sportingscout.scout_bank_backend.repositories.OrganizationsRepository;
import org.sportingscout.scout_bank_backend.mappers.CreateOrganizationRequest;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrganizationsService {
  private final OrganizationsRepository organizationsRepo;
  private static final Logger logger = LoggerFactory.getLogger(OrganizationsService.class);
  private final CreateOrganizationRequest requestMapper;

  public OrganizationsService(OrganizationsRepository organizationsRepo, CreateOrganizationRequest mapper) {
    this.organizationsRepo = organizationsRepo;
    this.requestMapper = mapper;
  }

  public Optional<Organization> getOrganizationById(Long id) {
    try {
      logger.debug("Attempting to fetch organization with id: {}", id);
      Optional<Organization> temp = organizationsRepo.findById(id);
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching organization with id {}: ", id, e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during organization fetch with id: {}", id, e);
      throw e;
    }
  }

  public List<Organization> getAllOrganizations() {
    try {
      logger.debug("Attempting to fetch all organization instances");
      List<Organization> temp = organizationsRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all organization instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all organizations fetch: ", e);
      throw e;
    }
  }

  public void createOrganization(CreateOrganizationRequest request) {
    try {
      Organization entity = requestMapper.toEntity(request);
      organizationsRepo.save(entity);
      logger.debug("Successfully created organization | ID: {}", entity.getId());
    } catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create organization with duplicate unique field (name): ", e.getMessage());
      throw new IllegalArgumentException("An organization with this name already exists.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during organization creation: ", e);
      throw e;
    }
  }

  public void deleteOrganization(Long id) {
    try {
      logger.debug("Attempting to delete organization with ID: {}", id);
      if (!organizationsRepo.existsById(id)) {
        logger.warn("Delete failed: organization with ID {} does not exist", id);
        throw new NoSuchElementException("Cannot delete: organization not found.");
      }

      organizationsRepo.deleteById(id);
      logger.info("Successfully deleted organization with ID: {}", id);

    } catch (DataAccessException e) {
      logger.error("Database error during deletion of organization with id: {}", id, e.getMessage());
      throw new RuntimeException("Could not reach PostgreSQL during deletion.");
    } catch (Exception e) {
      logger.error("Unexpected error during organization deletion: ", e);
      throw e;
    }
  }

  public void updateOrganizationInformation(Long id, String name, String description) {
    try {
      Organization existingOrganization = organizationsRepo.findById(id)
          .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
      existingOrganization.setName(name);
      existingOrganization.setDescription(description);

      organizationsRepo.save(existingOrganization);
    } catch (DataIntegrityViolationException e) {
      logger.error("Update failed - unique constraint violation for organization {}: ", id, e.getMessage());
      throw new IllegalArgumentException(
          "Name already in use by another user organization.");
    }
  }
}
