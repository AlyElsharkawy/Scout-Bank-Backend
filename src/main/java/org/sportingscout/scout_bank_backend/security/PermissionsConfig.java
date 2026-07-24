package org.sportingscout.scout_bank_backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import org.sportingscout.scout_bank_backend.repositories.UsersRepository;
import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

@Component("auth")
public class PermissionsConfig {
  private final UsersRepository usersRepo;

  public PermissionsConfig(UsersRepository usersRepo) {
    this.usersRepo = usersRepo;
  }

  public boolean has(String permission) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null)
      return false;

    return auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals(permission));
  }

  public boolean hasId(Long id) {
    ApplicationUser user = getAuthenticatedUser();
    if (user == null || user.getId() == null) {
      return false;
    }

    return user.getId().equals(id);
  }

  public ApplicationUser getAuthenticatedUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated()) {
      return null;
    }

    if (auth.getPrincipal() instanceof ApplicationUser appUser) {
      return appUser;
    }

    return null;
  }
}
