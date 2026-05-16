package org.sportingscout.scout_bank_backend.repositories;

import org.sportingscout.scout_bank_backend.entities.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRanksRepository extends JpaRepository<UserRank, Long> {
  Optional<UserRank> findById(String id);

  Optional<UserRank> findByName(String name);
}
