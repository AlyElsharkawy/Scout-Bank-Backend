package org.sportingscout.scout_bank_backend.controllers.users;

import org.sportingscout.scout_bank_backend.services.users.UserRolesService;
import org.sportingscout.scout_bank_backend.dtos.roles.CreateUserRoleRequest;
import org.sportingscout.scout_bank_backend.dtos.roles.UpdateUserRoleRequest;
import org.sportingscout.scout_bank_backend.entities.UserRole;
import org.sportingscout.scout_bank_backend.mappers.users.UpdateUserRoleRequestMapper;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
public class UserRolesController {
  private final UserRolesService service;

  public UserRolesController(UserRolesService service,
      UpdateUserRoleRequestMapper updateRequestMapper) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<UserRole>> getAllRoles() {
    return ResponseEntity.ok(service.getAllRoles());
  }

  @PostMapping
  @PreAuthorize("@auth.has('role:create')")
  public ResponseEntity<Void> createRole(@Valid @RequestBody CreateUserRoleRequest request) {
    service.createRole(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("@auth.has('role:edit')")
  public ResponseEntity<Void> editRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
    service.updateRole(id, request);
    return ResponseEntity.noContent().build();
  }
}
