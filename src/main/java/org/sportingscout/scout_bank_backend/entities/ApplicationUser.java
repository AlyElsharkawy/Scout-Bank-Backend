package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;

@Getter
@Setter
@Entity
public class ApplicationUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Name cannot be empty or completely whitespace")
  private String name;

  private String profilePicture;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  @Pattern(regexp = "^01[0125]\\d{8}$", message = "Invalid Egyptian mobile number format")
  @NotBlank
  private String phoneNumber;

  @ManyToOne
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public Boolean hasBeenUpdated() {
    return this.createdAt == this.updatedAt;
  }

  @Builder
  public ApplicationUser(String name, String profilePicture, String email, String phoneNumber,
      Organization organization) {
    this.name = name;
    this.profilePicture = profilePicture;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.organization = organization;
  }
}
