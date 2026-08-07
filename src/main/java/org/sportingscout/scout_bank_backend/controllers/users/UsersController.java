package org.sportingscout.scout_bank_backend.controllers.users;

import jakarta.validation.Valid;
import org.sportingscout.scout_bank_backend.dtos.users.CreateUserRequest;
import org.sportingscout.scout_bank_backend.dtos.users.UpdateUserRequest;
import org.sportingscout.scout_bank_backend.dtos.users.AllUsersResponse;
import org.sportingscout.scout_bank_backend.dtos.users.SingleUserResponse;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.sportingscout.scout_bank_backend.mappers.users.SingleUserResponseMapper;
import org.sportingscout.scout_bank_backend.services.users.UsersService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
public class UsersController {
  private final UsersService usersService;
  private final SingleUserResponseMapper singleUserResponseMapper;

  public UsersController(UsersService usersService,
      SingleUserResponseMapper singleUserResponseMapper) {
    this.usersService = usersService;
    this.singleUserResponseMapper = singleUserResponseMapper;
  }

  @GetMapping
  @PreAuthorize("@auth.has('user:view')")
  public ResponseEntity<List<AllUsersResponse>> getAllUsers() {
    return ResponseEntity.ok(this.usersService.getAllUsers());
  }

  @GetMapping("/{id}")
  @PreAuthorize("@auth.hasId(#id)")
  public ResponseEntity<SingleUserResponse> getUsers(@PathVariable Long id) {
    return usersService.getUserById(id)
        .map(user -> ResponseEntity.ok(singleUserResponseMapper.toResponse(user)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
    usersService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<Long> login() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Object principal = null;
    try {
      principal = auth.getPrincipal();
    } catch (Throwable e) {
      e.printStackTrace();
    }

    // Note: Yes, this code is terrible. I must admit it
    // However, it shall remain until the entire authentication scheme is refactored
    // to stateful tokens
    if (principal instanceof ApplicationUser springUser) {
      String email = springUser.getUsername();

      Optional<ApplicationUser> temp = usersService.getUserByEmail(email);
      System.out.println("Is Present: " + temp.isPresent());
      if (temp.isPresent()) {
        return ResponseEntity.ok(temp.get().getId());
      }
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
    usersService.updateUser(id, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    usersService.deleteUser(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
