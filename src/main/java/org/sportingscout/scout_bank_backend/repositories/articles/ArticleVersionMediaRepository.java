package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersionMedia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ArticleVersionMediaRepository extends JpaRepository<ArticleVersionMedia, Long> {
  Optional<ArticleVersionMedia> findById(Long id);

  Optional<ArticleVersionMedia> findByKey(String key);

  List<ArticleVersionMedia> findByKeyIn(List<String> keys);

  List<ArticleVersionMedia> findAllByKeyIn(List<String> keys);

  List<ArticleVersionMedia> findByFileNameContainingIgnoreCase(String keyword);

  boolean existsByKey(String key);
}
