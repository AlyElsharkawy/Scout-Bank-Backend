package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Organization {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false, unique = true)
  private UUID externalId;

  @NotBlank(message = "Name cannot be blank or completely whitespace")
  @Column(nullable = false, unique = true)
  private String name;

  @NotBlank(message = "Description cannot be blank or completely whitespace")
  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_id")
  private ApplicationUser createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by_id")
  private ApplicationUser updatedBy;

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

  @Builder
  public Organization(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
