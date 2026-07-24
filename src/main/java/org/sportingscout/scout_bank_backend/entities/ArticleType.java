package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ArticleType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Article type name cannot be empty or completely whitespace")
  @Column(nullable = false)
  private String name;

  @NotBlank(message = "Article type description cannot be empty or completely whitespace")
  @Column(nullable = false, length = 511)
  private String description;

  @CreatedDate
  private Instant createdAt;

  @LastModifiedDate
  private Instant updatedAt;

  @CreatedBy
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", updatable = false, nullable = false)
  private ApplicationUser createdBy;

  @LastModifiedBy
  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by", updatable = false, nullable = false)
  private ApplicationUser updatedBy;

  @JsonProperty("updatedBy")
  public String getUpdatedByName() {
    return this.updatedBy != null ? this.updatedBy.getName() : null;
  }

  @JsonProperty("createdBy")
  public String getCreatedByName() {
    return this.createdBy != null ? this.createdBy.getName() : null;
  }

  @Builder
  public ArticleType(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
