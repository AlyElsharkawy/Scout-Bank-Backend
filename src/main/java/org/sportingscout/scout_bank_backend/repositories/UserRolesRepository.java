package org.sportingscout.scout_bank_backend.repositories;

import org.sportingscout.scout_bank_backend.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRole, Long> {
  Optional<UserRole> findById(String id);

  Optional<UserRole> findByName(String name);
}
