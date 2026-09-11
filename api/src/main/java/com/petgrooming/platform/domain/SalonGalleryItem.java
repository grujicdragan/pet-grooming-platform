package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "salon_gallery")
public class SalonGalleryItem {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  private String title;
  private String caption;

  @Column(name = "media_url", nullable = false)
  private String mediaUrl;

  @Column(name = "alt_text")
  private String altText;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(name = "is_published", nullable = false)
  private boolean published;

  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getTitle() {
    return title;
  }

  public String getCaption() {
    return caption;
  }

  public String getMediaUrl() {
    return mediaUrl;
  }

  public String getAltText() {
    return altText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isPublished() {
    return published;
  }
}
