package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Version;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;

@Entity
@Getter
@Setter
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(updatable = false, nullable = false, unique = true)
  @Setter(AccessLevel.NONE)
  private UUID externalId;

  @Column(nullable = false, unique = true)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Version
  private Long version;

  @Column(nullable = false)
  private int majorVersion;

  @Column(nullable = false)
  private int minorVersion;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column(name = "image_key")
  @ElementCollection
  @CollectionTable(name = "article_images", joinColumns = @JoinColumn(name = "article_id"))
  private List<String> imageNames = new ArrayList<>();

  @Column(name = "video_key")
  @ElementCollection
  @CollectionTable(name = "article_videos", joinColumns = @JoinColumn(name = "article_id"))
  private List<String> videoNames = new ArrayList<>();

  public void addImage(String imageName) {
    if (imageName != null && !imageName.trim().isEmpty()) {
      this.imageNames.add(imageName);
    }
  }

  public void removeImage(String imageName) {
    this.imageNames.remove(imageName);
  }

  public void addVideo(String videoName) {
    if (videoName != null && !videoName.trim().isEmpty()) {
      this.imageNames.add(videoName);
    }
  }

  public void removeVideo(String videoName) {
    this.imageNames.remove(videoName);
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

  public Boolean hasBeenUpdated() {
    return this.createdAt == this.updatedAt;
  }
}
