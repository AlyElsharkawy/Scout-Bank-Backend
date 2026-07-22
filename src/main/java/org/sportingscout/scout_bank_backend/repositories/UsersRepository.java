package org.sportingscout.scout_bank_backend.repositories;

import org.sportingscout.scout_bank_backend.entities.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<ApplicationUser, Long> {
  Optional<ApplicationUser> findById(String id);

  @EntityGraph(attributePaths = { "role.authorities" })
  Optional<ApplicationUser> findByEmail(String email);
}
