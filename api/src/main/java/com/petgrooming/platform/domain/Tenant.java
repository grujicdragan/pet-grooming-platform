package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(nullable = false)
  private String name;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  public UUID getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public String getName() {
    return name;
  }

  public boolean isActive() {
    return active;
  }
}
