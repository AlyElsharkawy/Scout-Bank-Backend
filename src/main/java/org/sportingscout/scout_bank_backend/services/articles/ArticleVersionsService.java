package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.ArticleTag;
import org.sportingscout.scout_bank_backend.entities.ArticleType;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleVersionRequest;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTagsRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTypeRepository;
import org.sportingscout.scout_bank_backend.repositories.UsersRepository;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import com.github.f4b6a3.uuid.UuidCreator;

@Service
public class ArticleVersionsService {
  private final ArticleVersionRepository articleVersionRepo;
  private final ArticleTagsRepository articleTagsRepo;
  private final UsersRepository usersRepo;
  private final ArticleTypeRepository articleTypeRepo;
  private static final Logger logger = LoggerFactory.getLogger(ArticleVersionsService.class);

  public ArticleVersionsService(ArticleVersionRepository articleVersionRepo,
      ArticleTagsRepository articleTagsRepo,
      UsersRepository usersRepo,
      ArticleTypeRepository articleTypeRepo) {
    this.articleVersionRepo = articleVersionRepo;
    this.articleTagsRepo = articleTagsRepo;
    this.usersRepo = usersRepo;
    this.articleTypeRepo = articleTypeRepo;
  }

  public UUID createArticleVersion(CreateArticleVersionRequest request) {
    try {
      Set<ArticleTag> tags = request.tagIds().stream()
          .map(articleTagsRepo::getReferenceById)
          .collect(Collectors.toSet());

      Set<ApplicationUser> editors = request.editorIds().stream()
          .map(usersRepo::getReferenceById)
          .collect(Collectors.toSet());

      ArticleType articleType = articleTypeRepo.getReferenceById(request.articleTypeId());

      UUID externalId = UuidCreator.getTimeBased();
      ArticleVersion articleVersion = ArticleVersion.builder()
          .content(request.content())
          .title(request.title())
          .majorVersion(request.majorVersion())
          .minorVersion(request.minorVersion())
          .tags(tags)
          .editors(editors)
          .externalId(externalId)
          .type(articleType)
          .build();

      articleVersionRepo.save(articleVersion);

      return externalId;
    } catch (DataIntegrityViolationException e) {
      logger.warn("Attempted to create ArticleVersion with duplicate or null unique name or null description : ",
          e.getMessage());
      throw new IllegalArgumentException(
          "a required field is null or a unique value constraint is being violated.");
    } catch (DataAccessException e) {
      logger.error("CRITICAL: Could not reach PostgreSQL on server!", e);
      throw new RuntimeException("Service temporarily unavailable. Please try again later.");
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleVersion creation: ", e);
      throw e;
    }
  }

  public Optional<ArticleVersion> getArticleVersion(UUID externalId) {
    try {
      logger.debug("Attempting to fetch ArticleVersion with external id: {}", externalId);
      Optional<ArticleVersion> temp = articleVersionRepo.findByExternalId(externalId);
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching ArticleVersion with external id {}: ", externalId, e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during ArticleVersion fetch with external id: {}", externalId, e);
      throw e;
    }
  }

  public List<ArticleVersion> getAllArticleVersions() {
    try {
      logger.debug("Attempting to fetch all ArticleVersion instances");
      List<ArticleVersion> temp = articleVersionRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ArticleVersion instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all ArticleVersion fetch: ", e);
      throw e;
    }
  }

  public void approveArticleVersion(Long articleId, ApprovalStatus status) {
  }
}
