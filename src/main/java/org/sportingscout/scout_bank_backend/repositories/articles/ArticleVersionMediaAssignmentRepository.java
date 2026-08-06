package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersionMediaAssignment;
import org.sportingscout.scout_bank_backend.entities.ArticleVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;

public interface ArticleVersionMediaAssignmentRepository
    extends JpaRepository<ArticleVersionMediaAssignment, Long> {
  Optional<ArticleVersionMediaAssignment> findById(Long id);

  @EntityGraph(attributePaths = { "media" })
  List<ArticleVersionMediaAssignment> findAllByArticleVersion(ArticleVersion articleVersion);
}
