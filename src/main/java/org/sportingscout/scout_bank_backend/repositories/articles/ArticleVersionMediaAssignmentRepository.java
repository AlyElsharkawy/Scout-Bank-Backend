package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersionMediaAssignment;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;

public interface ArticleVersionMediaAssignmentRepository
    extends JpaRepository<ArticleVersionMediaAssignment, Long> {
  Optional<ArticleVersionMediaAssignment> findById(Long id);

  @EntityGraph(attributePaths = { "media" })
  List<ArticleVersionMediaAssignment> findAllByArticleVersion(ArticleVersion articleVersion);

  @Modifying
  @Transactional
  @Query(value = """
      INSERT INTO article_version_media_assignment (article_version_id, article_version_media_id,
        caption, assignment_date)
      SELECT :targetVersionId, article_version_media_id, caption, CURRENT_TIMESTAMP
      FROM article_version_media_assignment
      WHERE article_version_id = :sourceVersionId
      """, nativeQuery = true)
  int duplicateAssignmentsForVersion(@Param("sourceVersionId") Long sourceVersionId,
      @Param("targetVersionId") Long targetVersionId);
}
