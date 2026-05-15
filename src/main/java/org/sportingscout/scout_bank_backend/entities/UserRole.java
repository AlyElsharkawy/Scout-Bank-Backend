package org.sportingscout.scout_bank_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserRole {
  @Id
  private Long id;

  private String name;
}
