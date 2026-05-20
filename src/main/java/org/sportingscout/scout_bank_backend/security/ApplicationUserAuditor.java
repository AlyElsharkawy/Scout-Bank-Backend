package org.sportingscout.scout_bank_backend.security;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationUserAuditor implements AuditorAware<ApplicationUser> {
  private final PermissionsConfig permissionsConfig;

  public ApplicationUserAuditor(PermissionsConfig permissionsConfig) {
    this.permissionsConfig = permissionsConfig;
  }

  @Override
  public Optional<ApplicationUser> getCurrentAuditor() {
    return Optional.ofNullable(permissionsConfig.getAuthenticatedUser());
  }
}
