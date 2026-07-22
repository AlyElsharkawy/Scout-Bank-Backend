package org.sportingscout.scout_bank_backend.entities;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "article_version_tag")
@EntityListeners(AuditingEntityListener.class)
public class ArticleTag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @NotBlank(message = "Name cannot be empty or completely whitespace")
  private String name;

  @Column(nullable = false)
  @CreatedDate
  private Instant createdAt;

  @Column(nullable = false)
  @LastModifiedDate
  private Instant updatedAt;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", updatable = false, nullable = false)
  @CreatedBy
  private ApplicationUser createdBy;

  @JsonProperty("createdBy")
  public String getCreatedByName() {
    return createdBy != null ? createdBy.getName() : null;
  }

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by", nullable = false)
  @LastModifiedBy
  private ApplicationUser updatedBy;

  @JsonProperty("updatedBy")
  public String getUpdatedByName() {
    return updatedBy != null ? updatedBy.getName() : null;
  }

  public Boolean hasBeenUpdated() {
    return !this.createdAt.equals(this.updatedAt);
  }

  @Builder
  public ArticleTag(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ID: " + this.id.toString() + "\n");
    sb.append("Name: " + this.name + "\n");
    if (this.createdBy != null) {
      sb.append("Creator Name: " + this.createdBy.getName() + "\n");
    } else {
      sb.append("Creator Name: null\n");
    }
    if (this.updatedBy != null) {
      sb.append("Updater Name: " + this.updatedBy.getName() + "\n");
    } else {
      sb.append("Updated By: null\n");
    }
    sb.append("Created at: " + this.createdAt.toString() + "\n");
    sb.append("Updated at: " + this.updatedAt.toString() + "\n");

    return sb.toString();
  }
}
