package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  @Query("SELECT MAX(v.majorVersion) FROM ArticleVersion v WHERE v.externalId = :externalId")
  Optional<Integer> findMaxMajorVersionByExternalId(@Param("externalId") UUID externalId);

  @Query("SELECT MAX(v.minorVersion) FROM ArticleVersion v WHERE v.externalId = :externalId AND v.majorVersion = :majorVersion")
  Optional<Integer> findMaxMinorVersionByExternalIdAndMajorVersion(
      @Param("externalId") UUID externalId,
      @Param("majorVersion") Integer majorVersion);

  @Query("SELECT DISTINCT v FROM ArticleVersion v " +
      "LEFT JOIN FETCH v.editors " +
      "WHERE v.externalId = :externalId " +
      "ORDER BY v.majorVersion DESC, v.minorVersion DESC")
  List<ArticleVersion> fetchAllWithEditorsByExternalId(@Param("externalId") UUID externalId);
}
