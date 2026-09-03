package org.sportingscout.scout_bank_backend.services.articles;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import static org.sportingscout.scout_bank_backend.configuration.RedisNamespaces.*;

import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTypeRepository;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleTypeRequest;
import org.sportingscout.scout_bank_backend.entities.ArticleType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class ArticleTypesService {
  private final ArticleTypeRepository articleTypeRepo;
  private static final Logger logger = LoggerFactory.getLogger(ArticleTypesService.class);

  public ArticleTypesService(ArticleTypeRepository articleTypeRepo) {
    this.articleTypeRepo = articleTypeRepo;
  }

  @CacheEvict(value = ARTICLE_TYPES, key = "'all'")
  public void createArticleType(CreateArticleTypeRequest request) {
    try {
      ArticleType temp = new ArticleType(request.name(), request.description());
      this.articleTypeRepo.save(temp);
    } catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create ArticleType with duplicate or null unique name or null description : ",
          e.getMessage());
      throw new IllegalArgumentException("A ArticleType with this name already exists or description field is null.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleType creation: ", e);
      throw e;
    }
  }

  @Caching(evict = {
      @CacheEvict(value = ARTICLE_TYPES, key = "'all'"),
      @CacheEvict(value = ARTICLE_TYPES, key = "#id")
  })
  public void editArticleType(Long id, CreateArticleTypeRequest request) {
    try {
      Optional<ArticleType> existing = this.articleTypeRepo.findById(id);
      ArticleType temp = existing.get();
      temp.setName(request.name());
      temp.setDescription(request.description());
      this.articleTypeRepo.save(temp);
    } catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to edit ArticleType with duplicate unique name or null description: ", e.getMessage());
      throw new IllegalArgumentException(
          "A ArticleType with this name already exists or required description is null.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleType edit: ", e);
      throw e;
    }
  }

  @Cacheable(value = ARTICLE_TYPES, key = "'all'")
  public List<ArticleType> getAllArticleTypes() {
    try {
      logger.debug("Attempting to fetch all ArticleTag instances");
      List<ArticleType> temp = this.articleTypeRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ArticleType instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all ArticleType fetch: ", e);
      throw e;
    }
  }

  @Cacheable(value = ARTICLE_TYPES, key = "#id")
  public ArticleType getArticleType(Long id) {
    try {
      logger.debug("Attempting to fetch ArticleTag instance");
      ArticleType temp = this.articleTypeRepo.findById(id)
          .orElseThrow(() -> new NoSuchElementException("No element with id " + id + " exists"));
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ArticleType instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all ArticleType fetch: ", e);
      throw e;
    }
  }
}
