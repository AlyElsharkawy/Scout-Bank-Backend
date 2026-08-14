package org.sportingscout.scout_bank_backend.entities;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
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
import jakarta.persistence.FetchType;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @JsonIgnore
  private Long version;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "live_version_id")
  private ArticleVersion liveArticle;

  @Column(nullable = false, updatable = false)
  @CreatedDate
  private Instant createdAt;

  @Column(nullable = false, updatable = false)
  @LastModifiedDate
  private Instant updatedAt;

  @LastModifiedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by_id", updatable = false)
  @JsonIgnore
  private ApplicationUser updatedBy;

  @CreatedBy
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_id", updatable = false)
  @JsonIgnore
  private ApplicationUser createdBy;

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

  public Article(ArticleVersion article) {
    this.liveArticle = article;
  }
}
