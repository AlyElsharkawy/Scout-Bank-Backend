package org.sportingscout.scout_bank_backend.services.articles;

import org.sportingscout.scout_bank_backend.dtos.OperationResult;
import org.sportingscout.scout_bank_backend.security.PermissionsConfig;
import org.sportingscout.scout_bank_backend.services.S3Service;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.entities.ArticleVersionEditorModifyOptions;
import org.sportingscout.scout_bank_backend.entities.ArticleVersionMedia;
import org.sportingscout.scout_bank_backend.entities.ArticleVersionMediaAssignment;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.entities.ArticleTag;
import org.sportingscout.scout_bank_backend.entities.ArticleType;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleVersionRequest;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleVersionWithMedia;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTagsRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleTypeRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionMediaAssignmentRepository;
import org.sportingscout.scout_bank_backend.repositories.articles.ArticleVersionMediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

import org.aspectj.weaver.TemporaryTypeMunger;
import org.hibernate.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.curiousoddman.rgxgen.nodes.FinalSymbol;
import com.github.f4b6a3.uuid.UuidCreator;

record NextVersionPair(
    int majorVersion,
    int minorVersion) {
}

record UserResolutionResult(
    Set<ApplicationUser> users,
    List<UUID> missingIds) {
}

@Service
public class ArticleVersionsService {
  private final ArticleVersionRepository articleVersionRepo;
  private final ArticleTagsRepository articleTagsRepo;
  private final ArticleTypeRepository articleTypeRepo;
  private final PermissionsConfig permissionsConfig;
  private final ArticleVersionMediaRepository articleVersionMediaRepo;
  private final ArticleVersionMediaAssignmentRepository articleVersionMediaAssignmentRepo;
  private final S3Service s3Service;

  @PersistenceContext
  private EntityManager entityManager;

  private static final Logger logger = LoggerFactory.getLogger(ArticleVersionsService.class);

  public ArticleVersionsService(ArticleVersionRepository articleVersionRepo,
      ArticleTagsRepository articleTagsRepo,
      ArticleTypeRepository articleTypeRepo,
      PermissionsConfig permissionsConfig,
      ArticleVersionMediaRepository articleVersionMediaRepo,
      ArticleVersionMediaAssignmentRepository articleVersionMediaAssignmentRepo,
      S3Service s3Service) {
    this.articleVersionRepo = articleVersionRepo;
    this.articleTagsRepo = articleTagsRepo;
    this.articleTypeRepo = articleTypeRepo;
    this.permissionsConfig = permissionsConfig;
    this.articleVersionMediaRepo = articleVersionMediaRepo;
    this.articleVersionMediaAssignmentRepo = articleVersionMediaAssignmentRepo;
    this.s3Service = s3Service;
  }

  private UserResolutionResult resolveApplicationUserIds(List<UUID> ids) {
    Session session = entityManager.unwrap(Session.class);

    Map<UUID, Optional<ApplicationUser>> resolvedMap = ids.stream()
        .distinct()
        .collect(Collectors.toMap(
            id -> id,
            id -> session.bySimpleNaturalId(ApplicationUser.class).loadOptional(id)));

    Set<ApplicationUser> existingUsers = resolvedMap.values().stream()
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());

    List<UUID> missingIds = resolvedMap.entrySet().stream()
        .filter(entry -> entry.getValue().isEmpty())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    return new UserResolutionResult(existingUsers, missingIds);
  }

  /*
   * private Set<ApplicationUser> resolveApplicationUserIds(List<UUID> ids) {
   * Set<ApplicationUser> users = ids.stream()
   * .map(externalId -> entityManager.unwrap(Session.class)
   * .bySimpleNaturalId(ApplicationUser.class)
   * .getReference(externalId))
   * .collect(Collectors.toSet());
   * return users;
   * }
   */

  private NextVersionPair getNextVersion(boolean incrementMinor, UUID externalId) {
    String errorMessage = String.format("ArticleVersion subversion with externalId %s does not exist", externalId);
    Integer nextMajorNumber = this.articleVersionRepo.findMaxMajorVersionByExternalId(externalId)
        .orElseThrow(() -> new NoSuchElementException(errorMessage));
    if (!incrementMinor) {
      Integer nextMinorNumber = 0;
      return new NextVersionPair(nextMajorNumber + 1, nextMinorNumber);
    } else {
      Optional<Integer> minorNumberTemp = this.articleVersionRepo
          .findMaxMinorVersionByExternalIdAndMajorVersion(externalId, nextMajorNumber);

      Integer minorNumber = minorNumberTemp.isPresent() ? minorNumberTemp.get() + 1 : 0;

      return new NextVersionPair(nextMajorNumber, minorNumber);
    }
  }

  private void doUserAuthorOrEditorCheck(ApplicationUser user, UUID externalId,
      Integer majorVersion, Integer minorVersion) {
    Optional<ArticleVersion> subversion = this.articleVersionRepo.findByExternalIdAndMajorVersionAndMinorVersion(
        externalId,
        majorVersion, minorVersion);
    if (subversion.isPresent()) {
      Set<Long> editorIds = subversion.get().getEditors().stream()
          .map(ApplicationUser::getId)
          .collect(Collectors.toSet());

      Long authorId = subversion.get().getAuthor().getId();

      if (user.getId() != authorId && !editorIds.contains(user.getId())) {
        throw new AccessDeniedException(String.format(
            "Application User with external id %s is not allowed to access ArticleVersion subverion with external id %s because the user is not the author or an editor",
            user.getExternalId(), externalId));
      }
    } else {
      throw new NoSuchElementException(String.format(
          "ArticleVersion subversion with external id %s, majorVersion %d, and minorVersion %d does not exist",
          externalId.toString(), majorVersion, minorVersion));
    }
  }

  @Transactional
  private void saveAllMediaToDB(ArticleVersion articleVersion,
      List<CreateArticleVersionRequest.ArticleMediaRequest> mediaInput) {

    List<String> articleVersionMediaKeys = mediaInput.stream()
        .map(CreateArticleVersionRequest.ArticleMediaRequest::key)
        .toList();

    List<ArticleVersionMedia> articleVersionMedias = this.articleVersionMediaRepo
        .findAllByKeyIn(articleVersionMediaKeys);
    // System.out.println("Size: " + articleVersionMedias.size() + " should be: " +
    // mediaInput.size());

    // Check if all were valid
    if (articleVersionMedias.size() != mediaInput.size()) {
      throw new IllegalArgumentException("Non-existing media was added");
    }

    List<ArticleVersionMediaAssignment> mediaResult = new ArrayList<>(mediaInput.size());
    for (int i = 0; i < mediaInput.size(); i++) {
      ArticleVersionMediaAssignment temp = new ArticleVersionMediaAssignment(
          articleVersion, articleVersionMedias.get(i), mediaInput.get(i).caption());
      mediaResult.add(temp);
      // System.out.println("Temporary: " + temp.toString());
    }
    // System.out.println(mediaResult);
    this.articleVersionMediaAssignmentRepo.saveAll(mediaResult);
  }

  public UUID createArticleVersion(CreateArticleVersionRequest request) {
    try {
      Set<ArticleTag> tags = request.tagIds().stream()
          .map(articleTagsRepo::getReferenceById)
          .collect(Collectors.toSet());

      ArticleType articleType = articleTypeRepo.getReferenceById(request.articleTypeId());

      UUID externalId = UuidCreator.getTimeBased();
      ArticleVersion articleVersion = ArticleVersion.builder()
          .content(request.content())
          .title(request.title())
          .majorVersion(0)
          .minorVersion(1)
          .previousMinorVersion(0)
          .previousMajorVersion(0)
          .reviewer(null)
          .tags(tags)
          .updateNote(request.updateNote())
          .externalId(externalId)
          .type(articleType)
          .build();

      articleVersionRepo.save(articleVersion);

      List<CreateArticleVersionRequest.ArticleMediaRequest> articleVersionMedia = request.media();
      saveAllMediaToDB(articleVersion, articleVersionMedia);

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

  // TODO: Add Exception handling later
  public void updateArticleVersionSubversion(UUID externalId,
      Integer majorVersion, Integer minorVersion,
      CreateArticleVersionRequest request,
      boolean incrementMinor) {
    ArticleVersion previous = this.articleVersionRepo.findByExternalIdAndMajorVersionAndMinorVersion(externalId,
        majorVersion, minorVersion)
        .orElseThrow(() -> new NoSuchElementException(String.format(
            "ArticleVersion subversion with externalId %s, majorVersion %d, and minorVersion %d does not exist",
            externalId.toString(), majorVersion, minorVersion)));

    Set<ArticleTag> tags = request.tagIds().stream()
        .map(articleTagsRepo::getReferenceById)
        .collect(Collectors.toSet());

    NextVersionPair nextVersionPair = getNextVersion(incrementMinor, externalId);
    ArticleType articleType = articleTypeRepo.getReferenceById(request.articleTypeId());
    ArticleVersion newArticleVersion = ArticleVersion.builder()
        .externalId(previous.getExternalId())
        .content(request.content())
        .title(request.title())
        .majorVersion(nextVersionPair.majorVersion())
        .minorVersion(nextVersionPair.minorVersion())
        .previousMinorVersion(previous.getMinorVersion())
        .previousMajorVersion(previous.getMajorVersion())
        .reviewer(null)
        .tags(tags)
        .updateNote(request.updateNote())
        .type(articleType)
        .build();

    articleVersionRepo.save(newArticleVersion);
  }

  private void doAdminSupervisorRoleCheck() {
    // Check if user is an admin or super admin first
    ApplicationUser authenticatedUser = this.permissionsConfig.getAuthenticatedUser();
    String userRole = authenticatedUser.getRole().getName();
    if (!userRole.equals("Admin") && !userRole.equals("Supervisor")) {
      // If user is not admin or super admin, then throw unauthorized exception
      throw new AccessDeniedException("User is not Admin or Supervisor");
    }
  }

  public List<ArticleVersionWithMedia> getAllArticleVersionSubversionsByExternalId(UUID externalId) {
    doAdminSupervisorRoleCheck();
    try {
      logger.debug("Attempting to fetch all ArticleVersion instances with external id: {}", externalId);
      List<ArticleVersion> temp = articleVersionRepo
          .findAllByExternalIdOrderByMajorVersionDescMinorVersionDesc(externalId);

      if (temp.size() == 0) {
        throw new NoSuchElementException(
            String.format("ArticleVersion with externalId %s does not exist", externalId));
      }

      List<List<ArticleVersionMediaAssignment>> mediaAssignments = new ArrayList<List<ArticleVersionMediaAssignment>>(
          temp.size());

      for (int i = 0; i < temp.size(); i++) {
        mediaAssignments.add(
            this.articleVersionMediaAssignmentRepo
                .findAllByArticleVersion(temp.get(i)));
      }

      List<ArticleVersionWithMedia> result = new ArrayList<>(temp.size());
      for (int i = 0; i < mediaAssignments.size(); i++) {
        List<ArticleVersionWithMedia.Media> finalMedias = new ArrayList<>(mediaAssignments.get(i).size());
        for (int j = 0; j < mediaAssignments.get(i).size(); j++) {
          ArticleVersionWithMedia.Media tempMedia = new ArticleVersionWithMedia.Media(
              this.s3Service.getPresignedUrl(
                  mediaAssignments.get(i).get(j).getMedia().getKey()),
              mediaAssignments.get(i).get(j).getCaption(),
              mediaAssignments.get(i).get(j).getMedia().getFileName(),
              mediaAssignments.get(i).get(j).getMedia().getMimeType());
          finalMedias.add(tempMedia);
        }
        ArticleVersionWithMedia tempMedia = new ArticleVersionWithMedia(temp.get(i), finalMedias);
        System.out.println("Contents: " + tempMedia.toString());
        result.add(tempMedia);
      }
      return result;

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

  public ArticleVersionWithMedia getArticleVersionSubversion(UUID externalId, Integer majorVersion,
      Integer minorVersion) {
    ApplicationUser user = this.permissionsConfig.getAuthenticatedUser();
    doUserAuthorOrEditorCheck(user, externalId, majorVersion, minorVersion);
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
      if (temp.isEmpty()) {
        throw new NoSuchElementException(String.format(
            "ArticleVersion subversion with externalId %s, majorVersion %d, and minorVersion %d does not exist",
            externalId, majorVersion, minorVersion));
      }

      List<ArticleVersionMediaAssignment> tempMedia = this.articleVersionMediaAssignmentRepo
          .findAllByArticleVersion(temp.get());

      List<ArticleVersionWithMedia.Media> medias = new ArrayList<>(tempMedia.size());
      for (int i = 0; i < tempMedia.size(); i++) {
        medias.add(new ArticleVersionWithMedia.Media(
            this.s3Service.getPresignedUrl(tempMedia.get(i).getMedia().getKey()),
            tempMedia.get(i).getCaption(),
            tempMedia.get(i).getMedia().getFileName(),
            tempMedia.get(i).getMedia().getMimeType()));
      }

      return new ArticleVersionWithMedia(temp.get(), medias);
    } catch (DataAccessException e) {
      logger.error(
          "Database error while fetching ArticleVersion subversion instance with externalId {}, majorVersion {}, and minorVersion {}: ",
          externalId, majorVersion, minorVersion, e);
      throw e;
    } catch (Exception e) {
      logger.error(
          "Unexpected error during ArticleVersion subversion fetch with externalId {}, majorVersion {}, minorVersion {}: ",
          externalId, majorVersion, minorVersion, e);
      throw e;
    }
  }

  @Transactional
  public OperationResult<Void> modifyArticleVersionEditors(ArticleVersionEditorModifyOptions option,
      UUID externalId, Integer majorVersion, Integer minorVersion, List<String> editorIds) {

    // TODO: Customized exception handling will be added later

    UserResolutionResult resolution = resolveApplicationUserIds(
        editorIds.stream().map(UUID::fromString).toList());

    Set<ApplicationUser> editorUsers = resolution.users();
    List<UUID> missingIds = resolution.missingIds();

    List<String> warnings = new ArrayList<>();
    if (!missingIds.isEmpty()) {
      warnings.add("The following user IDs were not found and were skipped: " + missingIds);
    }

    if (option == ArticleVersionEditorModifyOptions.ADD
        || option == ArticleVersionEditorModifyOptions.DELETE) {
      ArticleVersion version = this.articleVersionRepo
          .findByExternalIdAndMajorVersionAndMinorVersion(externalId,
              majorVersion,
              minorVersion)
          .orElseThrow(() -> new NoSuchElementException(String.format(
              "ArticleVersion subversion with externalId %s, majorVersion %d, and minorVersion %d does not exist",
              externalId.toString(), majorVersion, minorVersion)));

      switch (option) {
        case ADD: {
          version.getEditors().addAll(editorUsers);
          this.articleVersionRepo.save(version);
          break;
        }

        case DELETE: {
          version.getEditors().removeAll(editorUsers);
          this.articleVersionRepo.save(version);
          break;
        }

        default: {
        }
      }
    } else {
      List<ArticleVersion> allArticleVersions = this.articleVersionRepo.fetchAllWithEditorsByExternalId(externalId);
      if (allArticleVersions.size() == 0) {
        throw new NoSuchElementException(String.format(
            "ArticleVersion with externalId %s does not exist", externalId));
      }
      switch (option) {
        case ADD_RECURSIVELY: {
          for (ArticleVersion articleVersion : allArticleVersions) {
            articleVersion.getEditors().addAll(editorUsers);
          }
          this.articleVersionRepo.saveAll(allArticleVersions);
          break;
        }

        case DELETE_RECURSIVELY: {
          for (ArticleVersion articleVersion : allArticleVersions) {
            articleVersion.getEditors().removeAll(editorUsers);
          }
          this.articleVersionRepo.saveAll(allArticleVersions);
          break;
        }

        default: {
        }
      }
    }
    return OperationResult.withWarnings(null, warnings);
  }

  public void modifyArticleVersionTags(boolean incrementMinor, boolean addTags,
      UUID externalId, Integer majorVersion, Integer minorVersion,
      List<Long> tagIds, String updateNote) {
    // If add
    ArticleVersion existing = this.articleVersionRepo
        .findByExternalIdAndMajorVersionAndMinorVersion(externalId, majorVersion, minorVersion)
        .orElseThrow(() -> new NoSuchElementException(String.format(
            "ArticleVersion subversion with externalId %s, majorVersion %d, and minorVersion %d does not exist",
            externalId, majorVersion, minorVersion)));

    ArticleVersion newSubversion = ArticleVersion.builder()
        .title(existing.getTitle())
        .content(existing.getContent())
        .majorVersion(existing.getMajorVersion())
        .minorVersion(existing.getMinorVersion() + 1)
        .externalId(existing.getExternalId())
        .editors(new HashSet<>(existing.getEditors()))
        .author(existing.getAuthor())
        .type(existing.getType())
        .updateNote(updateNote)
        .build();

    NextVersionPair nextVersionPair = getNextVersion(incrementMinor, externalId);
    newSubversion.setMajorVersion(nextVersionPair.majorVersion());
    newSubversion.setMinorVersion(nextVersionPair.minorVersion());

    newSubversion.setPreviousMajorVersion(existing.getMajorVersion());
    newSubversion.setPreviousMinorVersion(existing.getMinorVersion());

    Set<ArticleTag> newTags = tagIds.stream()
        .map(this.articleTagsRepo::getReferenceById)
        .collect(Collectors.toSet());

    if (addTags) {
      existing.getTags().addAll(newTags);
    } else {
      existing.getTags().removeAll(newTags);
    }

    newSubversion.setTags(new HashSet<>(existing.getTags()));
    // TODO: Add exception handling later
    this.articleVersionRepo.save(newSubversion);
  }
}
