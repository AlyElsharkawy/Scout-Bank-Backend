package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;

import jakarta.validation.constraints.NotBlank;

import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserRank {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @NotBlank(message = "Name cannot be empty or completely whitespace")
  private String name;

  @Column(nullable = false, length = 512)
  @NotBlank(message = "Description cannot be empty")
  private String description;

  private Integer minimumAge;

  private Integer maximumAge;

  @Builder
  public UserRank(String name, String description, Integer minimumAge, Integer maximumAge) {
    this.name = name;
    this.description = description;
    this.minimumAge = minimumAge;
    this.maximumAge = maximumAge;
  }

  public UserRank(String name, String description, Integer minimumAge) {
    this.name = name;
    this.description = description;
    this.minimumAge = minimumAge;
  }

  public UserRank(String name, String description) {
    this.name = name;
    this.description = description;
  }
}
