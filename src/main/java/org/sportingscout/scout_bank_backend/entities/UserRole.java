package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.HashSet;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class UserRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name cannot be empty or completely whitespace")
  @Column(nullable = false, unique = true)
  private String name;

  @NotBlank(message = "Description cannot be empty or completely whitespace")
  @Column(nullable = false, length = 1024)
  private String description;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_role_authorities", joinColumns = @JoinColumn(name = "user_role_id"))
  private Set<String> authorities = new HashSet<>();

  public UserRole(String name, String description, Set<String> authorities) {
    this.name = name;
    this.description = description;
    this.authorities = authorities;
  }
}
