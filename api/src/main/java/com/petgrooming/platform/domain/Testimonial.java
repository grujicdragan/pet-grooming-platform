package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "testimonials")
public class Testimonial {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "author_name", nullable = false)
  private String authorName;

  @Column(name = "author_role")
  private String authorRole;

  @Column(nullable = false)
  private String content;

  private Short rating;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(name = "is_published", nullable = false)
  private boolean published;

  public UUID getTenantId() {
    return tenantId;
  }

  public String getAuthorName() {
    return authorName;
  }

  public String getAuthorRole() {
    return authorRole;
  }

  public String getContent() {
    return content;
  }

  public Short getRating() {
    return rating;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public boolean isPublished() {
    return published;
  }
}
