package org.sportingscout.scout_bank_backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.User;

import org.sportingscout.scout_bank_backend.repositories.UsersRepository;

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
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return false;
    }

    Object principal = null;
    try {
      principal = auth.getPrincipal();
    } catch (Throwable e) {
      e.printStackTrace();
    }

    if (principal instanceof User springUser) {
      String email = springUser.getUsername();

      return usersRepo.findByEmail(email)
          .map(dbUser -> dbUser.getId().equals(id))
          .orElse(false);
    }
    return false;
  }
}
