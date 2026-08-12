package org.sportingscout.scout_bank_backend.repositories.articles;

import org.sportingscout.scout_bank_backend.entities.Article;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
  Optional<Article> findById(Long id);
}
