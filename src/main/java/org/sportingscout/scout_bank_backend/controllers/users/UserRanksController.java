package org.sportingscout.scout_bank_backend.controllers.users;

import org.sportingscout.scout_bank_backend.services.users.UserRanksService;
import org.sportingscout.scout_bank_backend.entities.UserRank;
import org.sportingscout.scout_bank_backend.dtos.ranks.CreateUserRankRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ranks")
public class UserRanksController {
  private final UserRanksService service;

  public UserRanksController(UserRanksService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<UserRank>> getAllRanks() {
    return ResponseEntity.ok(service.getAllRanks());
  }

  @PostMapping
  @PreAuthorize("@auth.has('rank:create')")
  public ResponseEntity<Void> createRank(@Valid @RequestBody CreateUserRankRequest request) {
    service.createRank(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("@auth.has('rank:edit')")
  public ResponseEntity<Void> updateRank(@PathVariable Long id, @Valid @RequestBody CreateUserRankRequest request) {
    service.editRank(id, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
