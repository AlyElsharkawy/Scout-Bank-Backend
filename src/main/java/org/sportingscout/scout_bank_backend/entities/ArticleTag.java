package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity(name = "article_version_tag")
public class ArticleTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Name cannot be empty or completely whitespace")
  private String name;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @ManyToOne
  @JoinColumn(name = "created_by", updatable = false, nullable = false)
  private ApplicationUser createdBy;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public Boolean hasBeenUpdated() {
    return !this.createdAt.equals(this.updatedAt);
  }

  public ArticleTag(String name, ApplicationUser creator) {
    this.name = name;
    this.createdBy = creator;
  }
}
