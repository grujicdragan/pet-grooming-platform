package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "services")
public class ServiceOffer {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(name = "duration_minutes", nullable = false)
  private int durationMinutes;

  private String icon;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "is_bookable_online", nullable = false)
  private boolean bookableOnline;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public String getIcon() {
    return icon;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isBookableOnline() {
    return bookableOnline;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
