package com.petgrooming.platform.service;

import com.petgrooming.platform.config.RlsSessionSupport;
import com.petgrooming.platform.domain.Location;
import com.petgrooming.platform.domain.SalonProfile;
import com.petgrooming.platform.domain.ServiceOffer;
import com.petgrooming.platform.domain.ServicePrice;
import com.petgrooming.platform.domain.Tenant;
import com.petgrooming.platform.dto.TenantSiteResponse;
import com.petgrooming.platform.dto.TenantSiteResponse.BrandingDto;
import com.petgrooming.platform.dto.TenantSiteResponse.FeatureDto;
import com.petgrooming.platform.dto.TenantSiteResponse.GalleryItemDto;
import com.petgrooming.platform.dto.TenantSiteResponse.LocationDto;
import com.petgrooming.platform.dto.TenantSiteResponse.SalonDto;
import com.petgrooming.platform.dto.TenantSiteResponse.ServiceDto;
import com.petgrooming.platform.dto.TenantSiteResponse.SiteSectionDto;
import com.petgrooming.platform.dto.TenantSiteResponse.SocialLinkDto;
import com.petgrooming.platform.dto.TenantSiteResponse.TestimonialDto;
import com.petgrooming.platform.repository.LocationRepository;
import com.petgrooming.platform.repository.SalonBrandingRepository;
import com.petgrooming.platform.repository.SalonGalleryRepository;
import com.petgrooming.platform.repository.SalonProfileRepository;
import com.petgrooming.platform.repository.SalonSocialLinkRepository;
import com.petgrooming.platform.repository.ServiceOfferRepository;
import com.petgrooming.platform.repository.ServicePriceRepository;
import com.petgrooming.platform.repository.SiteSectionRepository;
import com.petgrooming.platform.repository.TenantFeatureRepository;
import com.petgrooming.platform.repository.TenantRepository;
import com.petgrooming.platform.repository.TestimonialRepository;
import com.petgrooming.platform.web.NotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicTenantService {

  private final RlsSessionSupport rlsSessionSupport;
  private final TenantRepository tenantRepository;
  private final SalonProfileRepository salonProfileRepository;
  private final SalonBrandingRepository salonBrandingRepository;
  private final LocationRepository locationRepository;
  private final SalonSocialLinkRepository salonSocialLinkRepository;
  private final ServiceOfferRepository serviceOfferRepository;
  private final ServicePriceRepository servicePriceRepository;
  private final SalonGalleryRepository salonGalleryRepository;
  private final TestimonialRepository testimonialRepository;
  private final SiteSectionRepository siteSectionRepository;
  private final TenantFeatureRepository tenantFeatureRepository;

  public PublicTenantService(
      RlsSessionSupport rlsSessionSupport,
      TenantRepository tenantRepository,
      SalonProfileRepository salonProfileRepository,
      SalonBrandingRepository salonBrandingRepository,
      LocationRepository locationRepository,
      SalonSocialLinkRepository salonSocialLinkRepository,
      ServiceOfferRepository serviceOfferRepository,
      ServicePriceRepository servicePriceRepository,
      SalonGalleryRepository salonGalleryRepository,
      TestimonialRepository testimonialRepository,
      SiteSectionRepository siteSectionRepository,
      TenantFeatureRepository tenantFeatureRepository
  ) {
    this.rlsSessionSupport = rlsSessionSupport;
    this.tenantRepository = tenantRepository;
    this.salonProfileRepository = salonProfileRepository;
    this.salonBrandingRepository = salonBrandingRepository;
    this.locationRepository = locationRepository;
    this.salonSocialLinkRepository = salonSocialLinkRepository;
    this.serviceOfferRepository = serviceOfferRepository;
    this.servicePriceRepository = servicePriceRepository;
    this.salonGalleryRepository = salonGalleryRepository;
    this.testimonialRepository = testimonialRepository;
    this.siteSectionRepository = siteSectionRepository;
    this.tenantFeatureRepository = tenantFeatureRepository;
  }

  @Transactional
  public TenantSiteResponse getSiteBySlug(String slug) {
    rlsSessionSupport.enableBypass();

    Tenant tenant = tenantRepository.findBySlugIgnoreCaseAndActiveTrue(slug)
        .orElseThrow(() -> new NotFoundException("Tenant not found: " + slug));

    UUID tenantId = tenant.getId();
    SalonProfile profile = salonProfileRepository.findByTenantId(tenantId)
        .orElseThrow(() -> new NotFoundException("Salon profile missing for tenant: " + slug));

    Map<UUID, ServicePrice> priceByService = servicePriceRepository.findByTenantId(tenantId).stream()
        .collect(Collectors.toMap(ServicePrice::getServiceId, p -> p, (a, b) -> a));

    List<ServiceDto> services = serviceOfferRepository
        .findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(tenantId)
        .stream()
        .map(service -> toServiceDto(service, priceByService.get(service.getId())))
        .toList();

    Location primaryLocation = locationRepository
        .findActiveByTenantOrdered(tenantId)
        .stream()
        .findFirst()
        .orElse(null);

    Map<String, SiteSectionDto> sections = new LinkedHashMap<>();
    siteSectionRepository.findByTenantIdAndVisibleTrueOrderByDisplayOrderAsc(tenantId)
        .forEach(section -> sections.put(
            section.getSectionKey(),
            new SiteSectionDto(
                section.getTitle(),
                section.getSubtitle(),
                section.getBody(),
                section.getContentJson(),
                section.getImageUrl(),
                section.getCtaLabel(),
                section.getCtaUrl()
            )
        ));

    Map<String, FeatureDto> features = new LinkedHashMap<>();
    tenantFeatureRepository.findByTenantId(tenantId)
        .forEach(feature -> features.put(
            feature.getFeatureKey(),
            new FeatureDto(feature.isEnabled(), feature.getConfigJson())
        ));

    BrandingDto branding = salonBrandingRepository.findByTenantId(tenantId)
        .map(b -> new BrandingDto(
            b.getPrimaryColor(),
            b.getSecondaryColor(),
            b.getAccentColor(),
            b.getLogoUrl(),
            b.getCoverImageUrl(),
            b.getFontFamily()
        ))
        .orElse(null);

    return new TenantSiteResponse(
        tenant.getSlug(),
        tenant.getName(),
        toSalonDto(profile),
        branding,
        primaryLocation == null ? null : toLocationDto(primaryLocation),
        salonSocialLinkRepository.findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(tenantId)
            .stream()
            .map(link -> new SocialLinkDto(link.getPlatform(), link.getUrl(), link.getHandle()))
            .toList(),
        services,
        salonGalleryRepository.findByTenantIdAndPublishedTrueOrderByDisplayOrderAsc(tenantId)
            .stream()
            .map(item -> new GalleryItemDto(
                item.getMediaUrl(),
                item.getAltText() != null ? item.getAltText() : item.getTitle(),
                item.getCaption()
            ))
            .toList(),
        testimonialRepository.findByTenantIdAndPublishedTrueOrderByDisplayOrderAsc(tenantId)
            .stream()
            .map(item -> new TestimonialDto(
                item.getContent(),
                item.getAuthorName(),
                item.getAuthorRole(),
                item.getAvatarUrl()
            ))
            .toList(),
        sections,
        features
    );
  }

  private static SalonDto toSalonDto(SalonProfile profile) {
    String phone = profile.getPhone();
    String phoneHref = phone == null || phone.isBlank()
        ? null
        : (phone.startsWith("tel:") ? phone : "tel:" + phone);

    String address = joinAddress(profile.getAddressLine1(), profile.getState(), profile.getCity());

    return new SalonDto(
        profile.getDisplayName(),
        profile.getTagline(),
        profile.getDescription(),
        profile.getPhone(),
        profile.getPhoneDisplay() != null ? profile.getPhoneDisplay() : profile.getPhone(),
        phoneHref,
        profile.getEmail(),
        address,
        profile.getAddressLine1(),
        profile.getCity(),
        profile.getState(),
        profile.getCountry(),
        profile.getTimezone(),
        profile.getMapsSearchUrl(),
        profile.getMapEmbedUrl()
    );
  }

  private static LocationDto toLocationDto(Location location) {
    return new LocationDto(
        location.getName(),
        location.getPhone(),
        location.getAddressLine1(),
        location.getCity(),
        location.getState(),
        location.getCountry()
    );
  }

  private static ServiceDto toServiceDto(ServiceOffer service, ServicePrice price) {
    int amount = price != null ? price.getPriceCents() : 0;
    String currency = price != null ? price.getCurrency() : "RSD";
    return new ServiceDto(
        service.getCode(),
        service.getCode(),
        service.getName(),
        service.getDescription(),
        service.getDurationMinutes(),
        amount,
        currency,
        service.getIcon()
    );
  }

  private static String joinAddress(String line1, String state, String city) {
    StringBuilder sb = new StringBuilder();
    if (line1 != null && !line1.isBlank()) {
      sb.append(line1);
    }
    if (state != null && !state.isBlank()) {
      if (!sb.isEmpty()) {
        sb.append(", ");
      }
      sb.append(state);
    }
    if (city != null && !city.isBlank()) {
      if (!sb.isEmpty()) {
        sb.append(", ");
      }
      sb.append(city);
    }
    return sb.toString();
  }
}
