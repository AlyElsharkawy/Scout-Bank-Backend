package org.sportingscout.scout_bank_backend.repositories;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<ApplicationUser, Long> {
  Optional<ApplicationUser> findById(String id);

  Optional<ApplicationUser> findByEmail(String email);
}
