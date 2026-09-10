package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false)
  private String name;

  private String phone;

  @Column(name = "address_line1", nullable = false)
  private String addressLine1;

  private String city;
  private String state;
  private String country;

  @Column(name = "is_primary", nullable = false)
  private boolean primaryLocation;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getName() {
    return name;
  }

  public String getPhone() {
    return phone;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getCountry() {
    return country;
  }

  public boolean isPrimaryLocation() {
    return primaryLocation;
  }

  public boolean isActive() {
    return active;
  }
}
