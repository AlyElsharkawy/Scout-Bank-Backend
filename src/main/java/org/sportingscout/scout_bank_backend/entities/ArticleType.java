package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Entity
public class ArticleType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Article type name cannot be empty or completely whitespace")
  @Column(nullable = false)
  private String name;

  @NotBlank(message = "Article type description cannot be empty or completely whitespace")
  @Column(nullable = false, length = 511)
  private String description;

  @Builder
  public ArticleType(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
