package org.sportingscout.scout_bank_backend.controllers.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.sportingscout.scout_bank_backend.services.articles.ArticleVersionsService;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleVersionRequest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/versions")
public class ArticleVersionController {

  private final ArticleVersionsService service;

  public ArticleVersionController(ArticleVersionsService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<ArticleVersion>> getAllArticleVersions() {
    return ResponseEntity.ok(service.getAllArticleVersions());
  }

  @GetMapping("/{externalId}")
  public ResponseEntity<ArticleVersion> getArticleVersions(@PathVariable String externalId) {
    return service.getArticleVersion(UUID.fromString(externalId))
        .map(articleVersion -> ResponseEntity.ok(articleVersion))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<String> createArticleVersion(@RequestBody @Valid CreateArticleVersionRequest request) {
    UUID externalid = service.createArticleVersion(request);
    return ResponseEntity.ok(externalid.toString());
  }
}
