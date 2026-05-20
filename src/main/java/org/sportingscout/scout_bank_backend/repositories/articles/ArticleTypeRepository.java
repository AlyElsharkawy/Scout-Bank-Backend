package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.ArticleType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleTypeRepository extends JpaRepository<ArticleType, Long> {
  Optional<ArticleType> findById(Long id);

  Optional<ArticleType> findByName(String name);
}
