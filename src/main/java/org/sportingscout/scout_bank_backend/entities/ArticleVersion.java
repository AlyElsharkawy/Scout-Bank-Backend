package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
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

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.time.Instant;

@Entity
@Getter
public class ArticleVersion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(updatable = false, nullable = false, unique = true)
  private UUID externalId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false, updatable = false)
  private ApplicationUser author;

  // Many to Many
  @ManyToMany
  @JoinTable(name = "article_version_editors", joinColumns = @JoinColumn(name = "article_version_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<ApplicationUser> editors = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @Setter
  private ApplicationUser reviewer;

  @Enumerated(EnumType.STRING)
  @Setter
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
  @Setter
  private Instant updatedAt;

  @ManyToMany
  @JoinTable(name = "article_version_tags", joinColumns = @JoinColumn(name = "article_version_id"), inverseJoinColumns = @JoinColumn(name = "article_tag_id"))
  private Set<ArticleTag> tags = new HashSet<>();

  @Column(name = "image_key")
  @ElementCollection
  @CollectionTable(name = "article_version_images", joinColumns = @JoinColumn(name = "article_version_id"))
  private Set<String> imageNames = new HashSet<>();

  @Column(name = "video_key")
  @ElementCollection
  @CollectionTable(name = "article_version_videos", joinColumns = @JoinColumn(name = "article_version_id"))
  private Set<String> videoNames = new HashSet<>();

  public Set<ArticleTag> getTags() {
    return Collections.unmodifiableSet(tags);
  }

  public Set<String> getImageNames() {
    return Collections.unmodifiableSet(imageNames);
  }

  @PrePersist
  protected void onCreate() {
    if (this.externalId == null) {
      this.externalId = UUID.randomUUID();
    }
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public String getSemanticVersion() {
    return this.majorVersion + "." + this.minorVersion;
  }

  public ArticleVersion(ApplicationUser author, Set<ApplicationUser> editors, String title, String content,
      int majorVersion, int minorVersion, Set<ArticleTag> tags, Set<String> imageNames, Set<String> videoNames) {
    this.author = author;
    this.editors = editors;
    this.status = ApprovalStatus.DRAFT;
    this.title = title;
    this.content = content;
    this.minorVersion = minorVersion;
    this.majorVersion = majorVersion;
    this.tags = tags;
    this.videoNames = videoNames;
    this.imageNames = imageNames;
  }
}
