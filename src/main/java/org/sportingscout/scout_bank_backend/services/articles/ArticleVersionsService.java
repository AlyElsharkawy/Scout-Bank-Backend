package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.security.PermissionsConfig;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.entities.ArticleVersionEditorModifyOptions;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.ArticleTag;
import org.sportingscout.scout_bank_backend.entities.ArticleType;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleVersionRequest;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTagsRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTypeRepository;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

import org.hibernate.Session;

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
  private final ArticleTypeRepository articleTypeRepo;
  private final PermissionsConfig permissionsConfig;

  @PersistenceContext
  private EntityManager entityManager;

  private static final Logger logger = LoggerFactory.getLogger(ArticleVersionsService.class);

  public ArticleVersionsService(ArticleVersionRepository articleVersionRepo,
      ArticleTagsRepository articleTagsRepo,
      ArticleTypeRepository articleTypeRepo,
      PermissionsConfig permissionsConfig) {
    this.articleVersionRepo = articleVersionRepo;
    this.articleTagsRepo = articleTagsRepo;
    this.articleTypeRepo = articleTypeRepo;
    this.permissionsConfig = permissionsConfig;
  }

  private Set<ApplicationUser> resolveApplicationUserIds(List<UUID> ids) {
    Set<ApplicationUser> users = ids.stream()
        .map(externalId -> entityManager.unwrap(Session.class)
            .bySimpleNaturalId(ApplicationUser.class)
            .getReference(externalId))
        .collect(Collectors.toSet());
    return users;
  }

  public UUID createArticleVersion(CreateArticleVersionRequest request) {
    try {
      Set<ArticleTag> tags = request.tagIds().stream()
          .map(articleTagsRepo::getReferenceById)
          .collect(Collectors.toSet());

      Set<ApplicationUser> editors = resolveApplicationUserIds(request.editorIds());
      ArticleType articleType = articleTypeRepo.getReferenceById(request.articleTypeId());

      UUID externalId = UuidCreator.getTimeBased();
      ArticleVersion articleVersion = ArticleVersion.builder()
          .content(request.content())
          .title(request.title())
          .majorVersion(0)
          .minorVersion(0)
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

  private void doAdminSupervisorRoleCheck() {
    // Check if user is an admin or super admin first
    ApplicationUser authenticatedUser = this.permissionsConfig.getAuthenticatedUser();
    String userRole = authenticatedUser.getRole().getName();
    if (userRole != "Admin" || userRole != "Supervisor") {
      // If user is not admin or super admin, then throw unauthorized exception
      throw new AccessDeniedException("User is not Admin or Supervisor");
    }
  }

  public List<ArticleVersion> getAllArticleVersionSubversionsByExternalId(UUID externalId) {
    doAdminSupervisorRoleCheck();
    try {
      logger.debug("Attempting to fetch all ArticleVersion instances with external id: {}", externalId);
      List<ArticleVersion> temp = articleVersionRepo
          .findAllByExternalIdOrderByMajorVersionDescMinorVersionDesc(externalId);
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ArticleVersion subversion instances with external id {}: ",
          externalId, e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all ArticleVersion subversion fetch: ", e);
      throw e;
    }
  }

  public List<ArticleVersion> getAllArticleVersionSubversions() {
    doAdminSupervisorRoleCheck();
    try {
      logger.debug("Attempting to fetch all ArticleVersion instances");
      List<ArticleVersion> temp = articleVersionRepo.findAll();
      return temp;
    } catch (DataAccessException e) {
      logger.error("Database error while fetching all ArticleVersion subversion instances: ", e);
      throw e;
    } catch (Exception e) {
      logger.error("Unexpected error during all ArticleVersion subversion fetch: ", e);
      throw e;
    }
  }

  public Optional<ArticleVersion> getArticleVersionSubversion(UUID externalId, Integer majorVersion,
      Integer minorVersion) {
    try {
      logger.debug("Attempting to fetch ArticleVersion subversion with externalId {}, majorNumber {}, minorNumber {}",
          externalId, majorVersion, minorVersion);
      Optional<ArticleVersion> temp;
      if (majorVersion != null && minorVersion != null) {
        temp = articleVersionRepo.findByExternalIdAndMajorVersionAndMinorVersion(externalId,
            majorVersion, minorVersion);
      } else {
        temp = articleVersionRepo.findFirstByExternalIdOrderByMajorVersionDescMinorVersionDesc(externalId);
      }
      return temp;
    } catch (DataAccessException e) {
      logger.error(
          "Database error while fetching ArticleVersion subversion instance with externalId {}, majorVersion {}, and minorVersion {}: ",
          externalId, majorVersion, minorVersion, e);
      throw e;
    } catch (Exception e) {
      logger.error(
          "Unexpected error during all ArticleVersion subversion with externalId {}, majorVersion {}, minorVersion {}: ",
          externalId, majorVersion, minorVersion, e);
      throw e;
    }
  }

  public void modifyArticleVersionEditors(ArticleVersionEditorModifyOptions option,
      UUID externalId, List<String> editorIds) {
    switch (option) {
      case ADD: {
        break;
      }
      case DELETE: {
        break;
      }
      case ADD_RECURSIVELY: {
        break;
      }
      case DELETE_RECURSIVELY: {
        break;
      }
    }
  }

  public void modifyArticleVersionTags(boolean operation, UUID externalId,
      List<Long> tags) {
    // If add
    if (operation == true) {
    }
    // If not, then remove
    else {
    }
  }
}
