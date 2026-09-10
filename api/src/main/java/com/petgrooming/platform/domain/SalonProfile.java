package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "salon_profile")
public class SalonProfile {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  private String tagline;
  private String description;
  private String phone;

  @Column(name = "phone_display")
  private String phoneDisplay;

  private String email;

  @Column(name = "address_line1")
  private String addressLine1;

  private String city;
  private String state;
  private String country;
  private String timezone;

  @Column(name = "maps_search_url")
  private String mapsSearchUrl;

  @Column(name = "map_embed_url")
  private String mapEmbedUrl;

  public UUID getId() {
    return id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getTagline() {
    return tagline;
  }

  public String getDescription() {
    return description;
  }

  public String getPhone() {
    return phone;
  }

  public String getPhoneDisplay() {
    return phoneDisplay;
  }

  public String getEmail() {
    return email;
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

  public String getTimezone() {
    return timezone;
  }

  public String getMapsSearchUrl() {
    return mapsSearchUrl;
  }

  public String getMapEmbedUrl() {
    return mapEmbedUrl;
  }
}
