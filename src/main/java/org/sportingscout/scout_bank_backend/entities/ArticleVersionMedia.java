package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ArticleVersionMedia {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploader_id", nullable = false, updatable = false)
  private ApplicationUser uploadedBy;

  // To make it a reference later
  @Column(nullable = false)
  private String mimeType;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false, unique = true)
  private String key;

  @Column(nullable = false)
  private String fileName;

  @Builder
  public ArticleVersionMedia(ApplicationUser uploadedBy, String mimeType,
      String key, String fileName) {
    this.uploadedBy = uploadedBy;
    this.mimeType = mimeType;
    this.key = key;
    this.fileName = fileName;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
  }
}
