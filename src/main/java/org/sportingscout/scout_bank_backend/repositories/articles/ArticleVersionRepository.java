package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ArticleVersionRepository extends JpaRepository<ArticleVersion, Long> {
  Optional<ArticleVersion> findById(Long id);

  Optional<ArticleVersion> findByExternalId(UUID id);

  Optional<ArticleVersion> findFirstByExternalIdOrderByMajorVersionDescMinorVersionDesc(UUID externalId);

  List<ArticleVersion> findAllByExternalIdOrderByMajorVersionDescMinorVersionDesc(UUID externalId);

  Optional<ArticleVersion> findByExternalIdAndMajorVersionAndMinorVersion(
      UUID externalId,
      int majorVersion,
      int minorVersion);

}
