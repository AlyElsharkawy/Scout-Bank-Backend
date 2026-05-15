package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ApplicationUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotBlank(message = "Name cannot be empty or completely whitespace")
  private String name;

  private String profilePicture;

  @Column(nullable = false, unique = true)
  @Email(message = "Email must be valid")
  @NotBlank(message = "Email is required")
  private String email;

  @Column(nullable = false, unique = true)
  @Pattern(regexp = "^01[0125]\\d{8}$", message = "Invalid Egyptian mobile number format")
  @NotBlank
  private String phoneNumber;

  @ManyToOne
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "application_user_authorities", joinColumns = @JoinColumn(name = "application_user_id"))
  private Set<String> authorities = new HashSet<>();

  @Column(nullable = false)
  private String password;

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
  public ApplicationUser(String email, String password, String name, String profilePicture, String phoneNumber,
      Organization organization, Set<String> authorities) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.profilePicture = profilePicture;
    this.phoneNumber = phoneNumber;
    this.organization = organization;
    this.authorities = authorities;
  }
}
