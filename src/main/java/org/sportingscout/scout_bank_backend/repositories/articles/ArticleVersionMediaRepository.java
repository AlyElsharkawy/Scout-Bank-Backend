package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleVersionMedia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleVersionMediaRepository extends JpaRepository<ArticleVersionMedia, Long> {
  Optional<ArticleVersionMedia> findById(Long id);

}
