package org.sportingscout.scout_bank_backend.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
import jakarta.validation.constraints.Email;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

@Getter
@Setter
@Entity
@NoArgsConstructor
@NaturalIdCache
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ApplicationUser implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NaturalId
  @Column(unique = true, updatable = false, nullable = false)
  private UUID externalId;

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

  @Column(nullable = false)
  private LocalDate birthDate;

  @ManyToOne
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @ManyToOne
  @JoinColumn(name = "user_rank_id")
  private UserRank rank;

  @ManyToOne
  @JoinColumn(name = "user_role_id")
  private UserRole role;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant currentInstant = Instant.now();
    this.createdAt = currentInstant;
    this.updatedAt = currentInstant;
    this.externalId = UuidCreator.getTimeBased();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public Boolean hasBeenUpdated() {
    return this.createdAt == this.updatedAt;
  }

  // Security stuff
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    if (this.role == null || this.role.getAuthorities() == null) {
      return java.util.Collections.emptySet();
    }
    return this.role.getAuthorities().stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getUsername() {
    return this.email;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Builder
  public ApplicationUser(String email, String password, String name, String profilePicture,
      String phoneNumber, LocalDate birthdate, Organization organization, UserRank rank,
      UserRole role) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.profilePicture = profilePicture;
    this.phoneNumber = phoneNumber;
    this.birthDate = birthdate;
    this.organization = organization;
    this.rank = rank;
    this.role = role;
  }

  @Override
  public String toString() {
    String roleInfo = "null";
    String authoritiesInfo = "null";

    if (this.role != null) {
      roleInfo = "RolePresent(id=" + this.role.getId() + ")";
      try {
        authoritiesInfo = this.role.getAuthorities().toString();
      } catch (Throwable e) {
        e.printStackTrace();
        authoritiesInfo = "Error fetching authorities: " + e.getClass().getSimpleName();
      }
    }

    return "ApplicationUser {" +
        "\n  email='" + email + '\'' +
        ",\n  name='" + name + '\'' +
        ",\n  phoneNumber='" + phoneNumber + '\'' +
        ",\n  organization=" + (organization != null ? "OrgPresent" : "null") +
        ",\n  rank=" + (rank != null ? "RankPresent" : "null") +
        ",\n  role=" + roleInfo +
        ",\n  authorities=" + authoritiesInfo +
        "\n}";
  }
}
