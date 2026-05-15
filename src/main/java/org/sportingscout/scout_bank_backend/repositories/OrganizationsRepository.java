package org.sportingscout.scout_bank_backend.repositories;

import org.sportingscout.scout_bank_backend.entities.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrganizationsRepository extends JpaRepository<Organization, Long> {
  Optional<Organization> findById(String id);

  Optional<Organization> findByName(String name);
}
