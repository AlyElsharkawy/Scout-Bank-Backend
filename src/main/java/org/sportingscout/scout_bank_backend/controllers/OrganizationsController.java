package org.sportingscout.scout_bank_backend.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

import org.sportingscout.scout_bank_backend.entities.Organization;
import org.sportingscout.scout_bank_backend.services.OrganizationsService;
import org.sportingscout.scout_bank_backend.dtos.organizations.CreateOrganizationRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationsController {

  private final OrganizationsService service;

  public OrganizationsController(OrganizationsService service) {
    this.service = service;
  }

  @GetMapping
  public List<Organization> getAllOrganizations() {
    return service.getAllOrganizations();
  }

  @PostMapping
  @PreAuthorize("@auth.has('organization:create')")
  public ResponseEntity<Void> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
    service.createOrganization(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Organization> getOrganizationById(@PathVariable Long id) {
    return service.getOrganizationById(id)
        .map(organization -> ResponseEntity.ok(organization))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@auth.has('organization:delete')")
  public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
    service.deleteOrganization(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("@auth.has('organization:edit')")
  public ResponseEntity<Void> updateOrganization(@PathVariable Long id,
      @Valid @RequestBody CreateOrganizationRequest request) {
    service.updateOrganizationInformation(id, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleInternalError(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }
}
