package com.petgrooming.platform.dto;

import java.util.List;
import java.util.Map;

public record TenantSiteResponse(
    String slug,
    String name,
    /** Locale actually applied to the content (null = tenant default language). */
    String locale,
    SalonDto salon,
    BrandingDto branding,
    LocationDto location,
    List<SocialLinkDto> socialLinks,
    List<ServiceDto> services,
    List<GalleryItemDto> gallery,
    List<TestimonialDto> testimonials,
    Map<String, SiteSectionDto> sections,
    Map<String, FeatureDto> features
) {
  public record SalonDto(
      String displayName,
      String tagline,
      String description,
      String phone,
      String phoneDisplay,
      String phoneHref,
      String email,
      String address,
      String addressLine1,
      String city,
      String state,
      String country,
      String timezone,
      String mapsSearchUrl,
      String mapEmbedUrl
  ) {}

  public record BrandingDto(
      String primaryColor,
      String secondaryColor,
      String accentColor,
      String logoUrl,
      String coverImageUrl,
      String fontFamily
  ) {}

  public record LocationDto(
      String name,
      String phone,
      String addressLine1,
      String city,
      String state,
      String country
  ) {}

  public record SocialLinkDto(
      String platform,
      String url,
      String handle
  ) {}

  public record ServiceDto(
      String id,
      String code,
      String label,
      String blurb,
      int minutes,
      int price,
      String currency,
      String icon
  ) {}

  public record GalleryItemDto(
      String src,
      String alt,
      String caption
  ) {}

  public record TestimonialDto(
      String quote,
      String name,
      String pet,
      String photo
  ) {}

  public record SiteSectionDto(
      String title,
      String subtitle,
      String body,
      Map<String, Object> content,
      String imageUrl,
      String ctaLabel,
      String ctaUrl
  ) {}

  public record FeatureDto(
      boolean enabled,
      Map<String, Object> config
  ) {}
}
