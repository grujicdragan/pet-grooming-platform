package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "salon_branding")
public class SalonBranding {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "primary_color")
  private String primaryColor;

  @Column(name = "secondary_color")
  private String secondaryColor;

  @Column(name = "accent_color")
  private String accentColor;

  @Column(name = "logo_url")
  private String logoUrl;

  @Column(name = "cover_image_url")
  private String coverImageUrl;

  @Column(name = "font_family")
  private String fontFamily;

  public UUID getTenantId() {
    return tenantId;
  }

  public String getPrimaryColor() {
    return primaryColor;
  }

  public String getSecondaryColor() {
    return secondaryColor;
  }

  public String getAccentColor() {
    return accentColor;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
  }

  public String getFontFamily() {
    return fontFamily;
  }
}
