package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ArticleTagsRepository extends JpaRepository<ArticleTag, Long> {
  Optional<ArticleTag> findById(Long id);

  Optional<ArticleTag> findByName(String name);

  boolean existsByName(String name);

  void deleteByName(String name);

  @Query("SELECT t.id FROM article_version_tag t WHERE t.name = :name")
  Optional<Long> findIdByName(@Param("name") String name);
}
