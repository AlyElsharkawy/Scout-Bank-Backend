package org.sportingscout.scout_bank_backend.controllers.articles;

import org.sportingscout.scout_bank_backend.entities.Article;
import org.sportingscout.scout_bank_backend.services.articles.ArticleService;
import org.sportingscout.scout_bank_backend.dtos.articles.ArticleWithMedia;
import org.sportingscout.scout_bank_backend.dtos.articles.CachedArticlePage;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

record CreateArticleRequest(
    @NotNull(message = "ArticleVersion externalId is required") String externalId,
    @PositiveOrZero(message = "ArticleVersion majorNumbers cannot be below zero") Integer majorVersion,
    @Positive(message = "ArticleVersion minorNumbers must be at or above zero") Integer minorVersion) {
}

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
  private final ArticleService service;

  ArticleController(ArticleService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<CachedArticlePage> getAllArticles(
      @PageableDefault(page = 0, size = 20, sort = "liveArticle.createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    CachedArticlePage tempPage = this.service.getAllArticles(pageable);
    return ResponseEntity.ok(tempPage);
  }

  @PostMapping
  public ResponseEntity<Long> createArticle(@Valid @RequestBody CreateArticleRequest request) {
    return ResponseEntity.ok(this.service.createArticle(
        UUID.fromString(request.externalId()),
        request.majorVersion(), request.minorVersion()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
    this.service.deleteArticle(id);
    return ResponseEntity.noContent().build();
  }
}
