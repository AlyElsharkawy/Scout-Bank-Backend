package org.sportingscout.scout_bank_backend.controllers.articles;

import org.sportingscout.scout_bank_backend.services.articles.ArticleTypesService;
import org.sportingscout.scout_bank_backend.entities.ArticleType;
import org.sportingscout.scout_bank_backend.dtos.articles.CreateArticleTypeRequest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/types")
public class ArticleTypesController {
  private final ArticleTypesService service;

  public ArticleTypesController(ArticleTypesService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<ArticleType>> getAllArticleTypes() {
    return ResponseEntity.ok(service.getAllArticleTypes());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ArticleType> getArticleType(@PathVariable Long id) {
    return ResponseEntity.ok(service.getArticleType(id));
  }

  @PostMapping
  @PreAuthorize("@auth.has('type:create')")
  public ResponseEntity<Void> createArticleType(@Valid @RequestBody CreateArticleTypeRequest request) {
    service.createArticleType(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PatchMapping("/{id}")
  // @PreAuthorize("@auth.has('type:edit')")
  public ResponseEntity<Void> editArticleType(@PathVariable Long id,
      @Valid @RequestBody CreateArticleTypeRequest request) {
    service.editArticleType(id, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
