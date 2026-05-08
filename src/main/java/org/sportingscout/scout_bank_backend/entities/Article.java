package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Version;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.FetchType;

import java.util.UUID;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false, unique = true)
  private UUID externalId;

  @Version
  private Long version;

  @OneToOne
  @JoinColumn(name = "live_version_id")
  private ArticleVersion liveArticle;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false, updatable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @Setter
  private ApplicationUser updatedBy;

  @PrePersist
  protected void onCreate() {
    if (this.externalId == null) {
      this.externalId = UUID.randomUUID();
    }
    this.updatedAt = Instant.now();
    this.createdAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public Article(ArticleVersion article) {
    this.liveArticle = article;
  }
}
