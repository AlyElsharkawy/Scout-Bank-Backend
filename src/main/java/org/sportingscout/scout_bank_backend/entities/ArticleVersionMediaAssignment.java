package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@IdClass(ArticleVersionMediaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleVersionMediaAssignment {
  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_version_id", nullable = false)
  private ArticleVersion articleVersion;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_version_media_id", nullable = false)
  private ArticleVersionMedia media;

  @Column(nullable = true, length = 512)
  private String caption;

  @CreationTimestamp
  private Instant assignmentDate;

  @Override
  public String toString() {
    return "ArticleVersionMediaAssignment{" +
        "articleVersionId=" + (articleVersion != null ? articleVersion.getId() : null) +
        ", mediaId=" + (media != null ? media.getId() : null) +
        ", caption='" + caption + '\'' +
        '}';
  }

  public ArticleVersionMediaAssignment(ArticleVersion articleVersion,
      ArticleVersionMedia articleVersionMedia,
      String caption) {
    this.articleVersion = articleVersion;
    this.media = articleVersionMedia;
    this.caption = caption;
  }
}
