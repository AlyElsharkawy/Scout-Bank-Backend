package org.sportingscout.scout_bank_backend.entities;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Version;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
//import jakarta.persistence.PrePersist;
//import jakarta.persistence.PreUpdate;
import jakarta.persistence.FetchType;

import java.util.UUID;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // @Column(nullable = false, updatable = false, unique = true)
  // private UUID externalId;

  @Version
  private Long version;

  @OneToOne
  @JoinColumn(name = "live_version_id")
  private ArticleVersion liveArticle;

  @Column(nullable = false, updatable = false)
  @CreatedBy
  private Instant createdAt;

  @Column(nullable = false, updatable = false)
  @LastModifiedDate
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @LastModifiedBy
  private ApplicationUser updatedBy;

  @Column(columnDefinition = "TEXT")
  private String updateNotes;

  /*
   * @PrePersist
   * protected void onCreate() {
   * if (this.externalId == null) {
   * this.externalId = UUID.randomUUID();
   * }
   * Instant currentInstant = Instant.now();
   * this.updatedAt = currentInstant;
   * this.createdAt = currentInstant;
   * }
   * 
   * @PreUpdate
   * protected void onUpdate() {
   * this.updatedAt = Instant.now();
   * }
   */

  public Article(ArticleVersion article, String updateNotes) {
    this.liveArticle = article;
    this.updateNotes = updateNotes;
  }
}
