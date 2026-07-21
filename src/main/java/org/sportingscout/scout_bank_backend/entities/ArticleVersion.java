package org.sportingscout.scout_bank_backend.entities;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import lombok.Getter;
import lombok.Builder;
import lombok.Singular;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.time.Instant;

import com.github.f4b6a3.uuid.UuidCreator;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class ArticleVersion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(updatable = false, nullable = false, unique = true)
  private UUID externalId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false, updatable = false)
  @CreatedBy
  private ApplicationUser author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_type_id", nullable = false, updatable = false)
  private ArticleType type;

  // Many to Many
  @ManyToMany
  @JoinTable(name = "article_version_editors", joinColumns = @JoinColumn(name = "article_version_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Singular
  private Set<ApplicationUser> editors = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @LastModifiedBy
  private ApplicationUser reviewer;

  @Enumerated(EnumType.STRING)
  private ApprovalStatus status;

  @Column(nullable = false, updatable = false)
  @NotBlank(message = "Name cannot be empty or completely whitespace")
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, updatable = false)
  private int majorVersion;

  @Column(nullable = false, updatable = false)
  private int minorVersion;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @ManyToMany
  @JoinTable(name = "article_version_tags", joinColumns = @JoinColumn(name = "article_version_id"), inverseJoinColumns = @JoinColumn(name = "article_tag_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  @Singular
  private Set<ArticleTag> tags = new HashSet<>();

  public void setStatus(ApprovalStatus status, ApplicationUser reviewer) {
    this.reviewer = reviewer;
    this.status = status;
  }

  @PrePersist
  protected void onCreate() {
    if (this.externalId == null) {
      this.externalId = UuidCreator.getTimeOrdered();
    }
    Instant currentInstant = Instant.now();
    this.createdAt = currentInstant;
    this.updatedAt = currentInstant;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public String getSemanticVersion() {
    return this.majorVersion + "." + this.minorVersion;
  }

  @Builder
  public ArticleVersion(ApplicationUser author, ArticleType type, Set<ApplicationUser> editors, String title,
      String content,
      int majorVersion, int minorVersion, Set<ArticleTag> tags) {
    this.author = author;
    this.type = type;
    this.editors = editors;
    this.status = ApprovalStatus.DRAFT;
    this.title = title;
    this.content = content;
    this.minorVersion = minorVersion;
    this.majorVersion = majorVersion;
    this.tags = tags;
  }
}
