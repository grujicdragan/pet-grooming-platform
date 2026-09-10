package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "salon_social_links")
public class SalonSocialLink {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(nullable = false, length = 50)
  private String platform;

  @Column(nullable = false)
  private String url;

  private String handle;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  public UUID getTenantId() {
    return tenantId;
  }

  public String getPlatform() {
    return platform;
  }

  public String getUrl() {
    return url;
  }

  public String getHandle() {
    return handle;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isActive() {
    return active;
  }
}
