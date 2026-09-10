package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "site_sections")
public class SiteSection {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "section_key", nullable = false, length = 100)
  private String sectionKey;

  private String title;
  private String subtitle;
  private String body;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> contentJson;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "cta_label")
  private String ctaLabel;

  @Column(name = "cta_url")
  private String ctaUrl;

  @Column(name = "is_visible", nullable = false)
  private boolean visible;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  public UUID getTenantId() {
    return tenantId;
  }

  public String getSectionKey() {
    return sectionKey;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getBody() {
    return body;
  }

  public Map<String, Object> getContentJson() {
    return contentJson;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getCtaLabel() {
    return ctaLabel;
  }

  public String getCtaUrl() {
    return ctaUrl;
  }

  public boolean isVisible() {
    return visible;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
