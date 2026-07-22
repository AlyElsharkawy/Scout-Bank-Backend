package org.sportingscout.scout_bank_backend.services.articles;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.persistence.PersistenceContext;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;

import org.sportingscout.scout_bank_backend.entities.ArticleTag;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTagsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class ArticleTagsService {
  private ArticleTagsRepository tagsRepo;
  private static final Logger logger = LoggerFactory.getLogger(ArticleTagsService.class);

  public ArticleTagsService(ArticleTagsRepository tagsRepo) {
    this.tagsRepo = tagsRepo;
  }

  public List<ArticleTag> getAllTags() {
    try {
      logger.debug("Attempting to fetch all ArticleTag instances");
      List<ArticleTag> temp = tagsRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ArticleTag instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all ArticleTag fetch: ", e);
      throw e;
    }
  }

  public Optional<ArticleTag> getById(Long id) {
    try {
      logger.debug("Attempting to fetch ArticleTag with id: {}", id);
      Optional<ArticleTag> temp = tagsRepo.findById(id);
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching ArticleTag with id {}: ", id, e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleTag fetch with id: {}", id, e);
      throw e;
    }
  }

  public void deleteById(Long id) {
    try {
      logger.debug("Attempting to delete ArticleTag with ID: {}", id);
      if (!tagsRepo.existsById(id)) {
        logger.warn("Delete failed: ArticleTag with ID {} does not exist", id);
        throw new NoSuchElementException("Cannot delete: ArticleTag not found.");
      }

      tagsRepo.deleteById(id);
      logger.info("Successfully deleted ArticleTag with ID: {}", id);

    } catch (DataAccessException e) {
      logger.error("Database error during deletion of ArticleTag with id: {}", id, e.getMessage());
      throw new RuntimeException("Could not reach PostgreSQL during deletion.");
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleTag deletion: ", e);
      throw e;
    }
  }

  public void createTag(String name) {
    try {
      ArticleTag temp = ArticleTag.builder().name(name).build();
      tagsRepo.save(temp);
    }

    catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create ArticleTag with duplicate name or null name: ", e.getMessage());
      throw new IllegalArgumentException("An ArticleTag with this name already exists or a name is null.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleTag creation: ", e);
      throw e;
    }
  }

  public void deleteByName(String name) {
    try {
      logger.debug("Attempting to delete ArticleTag with Name: {}", name);
      if (!tagsRepo.existsByName(name)) {
        logger.warn("Delete failed: ArticleTag with Name {} does not exist", name);
        throw new NoSuchElementException("Cannot delete: ArticleTag not found.");
      }

      tagsRepo.deleteByName(name);
      logger.info("Successfully deleted ArticleTag with name: {}", name);

    } catch (DataAccessException e) {
      logger.error("Database error during deletion of ArticleTag with id: {}", name, e.getMessage());
      throw new RuntimeException("Could not reach PostgreSQL during deletion.");
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleTag deletion: ", e);
      throw e;
    }
  }

  @Transactional
  public void editTag(Long id, String newName) {
    ArticleTag existingTag = tagsRepo.findById(id)
        .orElseThrow(() -> new NoSuchElementException("ArticleTag not found with id: " + id));
    System.out.println("Existing tag: " + existingTag.toString());
    existingTag.setName(newName);
    System.out.println("New tag: " + existingTag.toString());

  }
}
