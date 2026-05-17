package org.sportingscout.scout_bank_backend.controllers.articles;

import org.sportingscout.scout_bank_backend.services.articles.ArticleTagsService;
import org.sportingscout.scout_bank_backend.entities.ArticleTag;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tags")
public class ArticleTagsController {
  private final ArticleTagsService service;

  public ArticleTagsController(ArticleTagsService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<ArticleTag>> getAllTags() {
    return ResponseEntity.ok(service.getAllTags());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ArticleTag> getTagById(@PathVariable Long id) {
    return service.getById(id)
        .map(user -> ResponseEntity.ok(user))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  @PreAuthorize("@auth.has('tag:create')")
  public ResponseEntity<Void> createTag(@Valid @RequestBody String name) {
    service.createTag(name);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@auth.has('tag:delete')")
  public ResponseEntity<ArticleTag> deleteTagById(@PathVariable Long id) {
    service.deleteById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("@auth.has('tag:edit')")
  public ResponseEntity<Void> updateTag(Long id, String newName) {
    service.editTag(id, newName);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
