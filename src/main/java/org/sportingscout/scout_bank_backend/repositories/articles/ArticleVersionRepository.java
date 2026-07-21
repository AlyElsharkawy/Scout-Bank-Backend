package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArticleVersionRepository extends JpaRepository<ArticleVersion, Long> {
  Optional<ArticleVersion> findById(Long id);

  Optional<ArticleVersion> findByExternalId(UUID id);

}
