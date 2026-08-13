package org.sportingscout.scout_bank_backend.controllers.articles;

import org.sportingscout.scout_bank_backend.entities.ApprovalStatus;
import org.sportingscout.scout_bank_backend.entities.ArticleVersionEditorModifyOptions;
import org.sportingscout.scout_bank_backend.services.articles.ArticleVersionsService;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleVersionWithMedia;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleVersionRequest;
import org.sportingscout.scout_bank_backend.dtos.OperationResult;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

record EditorRequest(
    @NotEmpty(message = "Editor list cannot be empty") List<@NotBlank(message = "UUID cannot be blank") @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "Every editor ID must be a valid UUID string") String> editorIds) {
}

record TagRequest(
    List<@Positive(message = "Tag IDs must be positive") Long> tagIds,
    @NotBlank(message = "Update Note cannot be blank") String updateNote) {
}

record ArticleMediaRequest(
    List<@NotBlank String> keys,
    List<@NotBlank String> captions,
    @NotBlank @Size(max = 511, message = "Update note cannot exceed 511 characters") String updateNote) {
}

record ArticleReviewRequest(
    @Size(max = 1023, message = "Review note cannot exceed 1023 characters") String reviewNote,
    @NotNull(message = "ApprovalStatus is required") ApprovalStatus status) {
}

@RestController
@RequestMapping("/api/versions")
public class ArticleVersionController {

  private final ArticleVersionsService service;

  public ArticleVersionController(ArticleVersionsService service) {
    this.service = service;
  }

  // This is called when you want to return all article version subversions
  // ...if you would ever want to do that
  @GetMapping("/all")
  public ResponseEntity<List<ArticleVersionWithMedia>> getAllArticleVersionSubversions(
      String externalId) {
    return ResponseEntity.ok(service.getAllArticleVersionSubversions());
  }

  // This is called when you want to return all article version subversions
  // for a particular externalid
  @GetMapping("/{externalId}/all")
  public ResponseEntity<List<ArticleVersionWithMedia>> getAllArticleVersionSubversionsByExternalId(
      @PathVariable String externalId) {
    return ResponseEntity.ok(service.getAllArticleVersionSubversionsByExternalId(UUID.fromString(externalId)));
  }

  // This is caalled when you want to get an article version by its external ID
  // By default, the latest subversion is returned
  // However, you can specify a specific version by query parameters
  @GetMapping("/{externalId}")
  public ResponseEntity<ArticleVersionWithMedia> getArticleVersionSubversion(
      @PathVariable String externalId,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion) {

    return ResponseEntity.ok(service.getArticleVersionSubversion(
        UUID.fromString(externalId),
        majorVersion,
        minorVersion));
  }

  // This is called when we create a new article version from scratch
  // The subversion created is 0.0
  @PostMapping
  @PreAuthorize("@auth.has('article:write')")
  public ResponseEntity<String> createArticleVersion(@RequestBody @Valid CreateArticleVersionRequest request) {
    UUID externalid = service.createArticleVersion(request);
    return ResponseEntity.ok(externalid.toString());
  }

  // This is called when we make changes to an existing articleVersion
  // DOES CREATE a new article version subversion
  @PatchMapping("/{externalId}")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<Void> updateArticleVersion(
      @PathVariable String externalId,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion,
      @RequestParam(name = "incrementMinor", defaultValue = "true") Boolean incrementMinor,
      @RequestBody CreateArticleVersionRequest request) {
    this.service.updateArticleVersionSubversion(UUID.fromString(externalId),
        majorVersion, minorVersion, request, incrementMinor);

    return ResponseEntity.ok().build();
  }

  // This is called when we want to add more tags to an already existing
  // article version subversion
  // DOES CREATE a new article version subversion
  @PatchMapping("/{externalId}/tags/modify")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<Void> addArticleVersionTags(
      @PathVariable String externalId,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion,
      @RequestParam(name = "incrementMinor", defaultValue = "true") Boolean incrementMinor,
      @RequestParam(name = "addTags") Boolean addTags,
      @Valid @RequestBody TagRequest request) {
    this.service.modifyArticleVersionTags(incrementMinor, addTags,
        UUID.fromString(externalId), majorVersion, minorVersion,
        request.tagIds(), request.updateNote());
    return ResponseEntity.ok().build();
  }

  // This is called when you want to add editor(s) to the article version
  // subversion
  // By default, it targets the latest article version subversion
  // DOES NOT CREATE a new article subversion
  @PatchMapping("/{externalId}/editors/modify")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<OperationResult<Void>> addArticleVersionEditors(
      @PathVariable String externalId,
      @Valid @RequestBody EditorRequest request,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion,
      @RequestParam(name = "addEditors") Boolean addEditors) {
    ArticleVersionEditorModifyOptions option = addEditors ? ArticleVersionEditorModifyOptions.ADD
        : ArticleVersionEditorModifyOptions.DELETE;
    this.service.modifyArticleVersionEditors(option,
        UUID.fromString(externalId), majorVersion, minorVersion,
        request.editorIds());

    OperationResult<Void> result = this.service.modifyArticleVersionEditors(option,
        UUID.fromString(externalId), majorVersion, minorVersion,
        request.editorIds());

    if (result.hasWarnings()) {
      return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    }

    return ResponseEntity.ok(result);
  }

  // This is called when you want to remove editor(s) to the article version
  // subversion
  // By default, it targets the latest article version subversion
  // DOES NOT CREATE a new article subversion
  @PatchMapping("/{externalId}/editors/modify/all")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<OperationResult<Void>> removeArticleVersionEditors(
      @PathVariable String externalId,
      @Valid @RequestBody EditorRequest request,
      @RequestParam(name = "addEditors") Boolean addEditors) {
    ArticleVersionEditorModifyOptions option = addEditors ? ArticleVersionEditorModifyOptions.ADD_RECURSIVELY
        : ArticleVersionEditorModifyOptions.DELETE_RECURSIVELY;
    this.service.modifyArticleVersionEditors(option,
        UUID.fromString(externalId), null, null,
        request.editorIds());

    OperationResult<Void> result = this.service.modifyArticleVersionEditors(option,
        UUID.fromString(externalId), null, null,
        request.editorIds());

    if (result.hasWarnings()) {
      return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    }

    return ResponseEntity.ok(result);
  }

  @PostMapping("/{externalId}/media")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<OperationResult<Void>> addMediaToArticleVersion(
      @PathVariable String externalId,
      @RequestParam(name = "addMedia", defaultValue = "true", required = false) Boolean addMedia,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion,
      @RequestParam(name = "incrementMinor", required = false, defaultValue = "true") Boolean incrementMinor,
      @Valid @RequestBody ArticleMediaRequest request) {

    if (addMedia) {
      return ResponseEntity.ok(
          this.service.addArticleMediaAssignment(
              UUID.fromString(externalId), majorVersion, minorVersion,
              incrementMinor, request.keys(), request.captions(), request.updateNote()));
    } else {
      return ResponseEntity.ok(
          this.service.removeArticleMediaAssignment(
              UUID.fromString(externalId), majorVersion, minorVersion, incrementMinor,
              request.keys(), request.updateNote()));
    }
  }

  // When you want to submit an article version subversion for review
  // DOES NOT create new ArticleVersion subversion
  @PatchMapping("/{externalId}/submit")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<Void> submitForReview(
      @PathVariable String externalId,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion) {
    this.service.submitArticleVersionSubversion(
        UUID.fromString(externalId), majorVersion, minorVersion);

    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{externalId}/review")
  @PreAuthorize("@auth.has('article:review')")
  public ResponseEntity<Void> reviewArticleVersionSubversion(
      @PathVariable String externalId,
      @RequestParam(name = "majorVersion") Integer majorVersion,
      @RequestParam(name = "minorVersion") Integer minorVersion,
      @Valid @RequestBody ArticleReviewRequest request) {

    this.service.reviewArticleVersion(UUID.fromString(externalId), majorVersion, minorVersion,
        request.reviewNote(), request.status());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{externalId}/review")
  @PreAuthorize("@auth.has('article:review')")
  public ResponseEntity<List<ArticleVersionWithMedia>> getAllArticleVersionSubversionsForReview(
      @PathVariable String externalId) {
    return ResponseEntity.ok(
        this.service.getAllArticleVersionsByStatus(
            UUID.fromString(externalId), ApprovalStatus.PENDING_REVIEW));
  }

  // You need edit permissions to view accepted articles since you won't be
  // calling this endpoint
  // unless you plan to change the live version of an Article
  @GetMapping("/{externalId}/approved")
  @PreAuthorize("@auth.has('article:edit')")
  public ResponseEntity<List<ArticleVersionWithMedia>> getAllAAcceptedArticleVersionSubversions(
      @PathVariable String externalId) {

    return ResponseEntity.ok(this.service.getAllArticleVersionsByStatus(
        UUID.fromString(externalId), ApprovalStatus.APPROVED));
  }
}
