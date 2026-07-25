package org.sportingscout.scout_bank_backend.entities;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.boot.security.autoconfigure.SecurityProperties.User;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Cacheable;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Builder;
import lombok.Singular;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

//import com.github.f4b6a3.uuid.UuidCreator;
record UserSummary(
    String name,
    String organizationName,
    String rankName,
    String roleName) {
}

@Entity
@Table(name = "article_version", uniqueConstraints = {
    @UniqueConstraint(name = "unique_article_version_subversion", columnNames = { "id", "major_version",
        "minor_version" })
})
@EntityListeners(AuditingEntityListener.class)
@NaturalIdCache
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleVersion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NaturalId
  @Column(updatable = false, nullable = false)
  private UUID externalId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false, updatable = false)
  @CreatedBy
  @JsonIgnore
  private ApplicationUser author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_type_id", nullable = false, updatable = false)
  @JsonIgnore
  private ArticleType type;

  // Many to Many
  @ManyToMany
  @JoinTable(name = "article_version_editors", joinColumns = @JoinColumn(name = "article_version_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Singular
  @JsonIgnore
  private Set<ApplicationUser> editors = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @LastModifiedBy
  @JsonIgnore
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
  @CreatedDate
  private Instant createdAt;

  @Column(nullable = false)
  @LastModifiedDate
  private Instant updatedAt;

  @ManyToMany
  @JoinTable(name = "article_version_tags", joinColumns = @JoinColumn(name = "article_version_id"), inverseJoinColumns = @JoinColumn(name = "article_tag_id"))
  @OnDelete(action = OnDeleteAction.CASCADE)
  @Singular
  @JsonIgnore
  private Set<ArticleTag> tags = new HashSet<>();

  @JsonProperty("author")
  public UserSummary getAuthorInformation() {
    return new UserSummary(
        this.author.getName(),
        this.author.getOrganization().getName(),
        this.author.getRank().getName(),
        this.author.getRole().getName());
  }

  @JsonProperty("editors")
  public List<UserSummary> getEditorsInformation() {
    List<UserSummary> summaries = new ArrayList<>(this.editors.size());
    for (ApplicationUser editor : this.editors) {
      summaries.add(new UserSummary(
          editor.getName(),
          editor.getOrganization().getName(),
          editor.getRank().getName(),
          editor.getRole().getName()));
    }
    return summaries;
  }

  @JsonProperty("reviewer")
  public UserSummary getReviewerInformation() {
    if (this.reviewer != null) {
      UserSummary result = new UserSummary(
          this.reviewer.getName(),
          this.reviewer.getOrganization().getName(),
          this.reviewer.getRank().getName(),
          this.reviewer.getRole().getName());
      return result;
    } else {
      return null;
    }
  }

  @JsonProperty("tags")
  public List<String> getTagNames() {
    if (this.tags != null && this.tags.size() != 0) {
      List<String> tagNames = new ArrayList<>(this.tags.size());
      for (ArticleTag tag : this.tags) {
        tagNames.add(tag.getName());
      }
      return tagNames;
    }
    return null;
  }

  @JsonProperty("type")
  public String getArticleType() {
    return this.type.getName();
  }

  public void setStatus(ApprovalStatus status, ApplicationUser reviewer) {
    this.reviewer = reviewer;
    this.status = status;
  }

  public String getSemanticVersion() {
    return this.majorVersion + "." + this.minorVersion;
  }

  @Builder
  public ArticleVersion(ApplicationUser author, ArticleType type, Set<ApplicationUser> editors,
      String title, String content,
      int majorVersion, int minorVersion,
      Set<ArticleTag> tags,
      UUID externalId) {
    this.author = author;
    this.type = type;
    this.editors = editors;
    this.status = ApprovalStatus.DRAFT;
    this.title = title;
    this.content = content;
    this.minorVersion = minorVersion;
    this.majorVersion = majorVersion;
    this.tags = tags;
    this.externalId = externalId;
  }
}
