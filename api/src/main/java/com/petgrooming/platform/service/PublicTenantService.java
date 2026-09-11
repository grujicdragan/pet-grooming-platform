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
import com.petgrooming.platform.repository.ContentTranslationRepository;
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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicTenantService {

  private static final String LOCALIZATION_FEATURE = "localization";

  private final RlsSessionSupport rlsSessionSupport;
  private final ContentTranslationRepository contentTranslationRepository;
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
      ContentTranslationRepository contentTranslationRepository,
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
    this.contentTranslationRepository = contentTranslationRepository;
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
    return getSiteBySlug(slug, null);
  }

  @Transactional
  public TenantSiteResponse getSiteBySlug(String slug, String requestedLocale) {
    rlsSessionSupport.enableBypass();

    Tenant tenant = tenantRepository.findBySlugIgnoreCaseAndActiveTrue(slug)
        .orElseThrow(() -> new NotFoundException("Tenant not found: " + slug));

    UUID tenantId = tenant.getId();
    SalonProfile profile = salonProfileRepository.findByTenantId(tenantId)
        .orElseThrow(() -> new NotFoundException("Salon profile missing for tenant: " + slug));

    Map<String, FeatureDto> features = new LinkedHashMap<>();
    tenantFeatureRepository.findByTenantId(tenantId)
        .forEach(feature -> features.put(
            feature.getFeatureKey(),
            new FeatureDto(feature.isEnabled(), feature.getConfigJson())
        ));

    String locale = resolveLocale(requestedLocale, features.get(LOCALIZATION_FEATURE));
    ContentTranslator tr = locale == null
        ? ContentTranslator.none()
        : ContentTranslator.of(contentTranslationRepository.findByTenantIdAndLocale(tenantId, locale));

    Map<UUID, ServicePrice> priceByService = servicePriceRepository.findByTenantId(tenantId).stream()
        .collect(Collectors.toMap(ServicePrice::getServiceId, p -> p, (a, b) -> a));

    List<ServiceDto> services = serviceOfferRepository
        .findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(tenantId)
        .stream()
        .map(service -> toServiceDto(service, priceByService.get(service.getId()), tr))
        .toList();

    Location primaryLocation = locationRepository
        .findActiveByTenantOrdered(tenantId)
        .stream()
        .findFirst()
        .orElse(null);

    Map<String, SiteSectionDto> sections = new LinkedHashMap<>();
    siteSectionRepository.findByTenantIdAndVisibleTrueOrderByDisplayOrderAsc(tenantId)
        .forEach(section -> {
          UUID id = section.getId();
          String type = ContentTranslator.SITE_SECTION;
          sections.put(
              section.getSectionKey(),
              new SiteSectionDto(
                  tr.text(type, id, "title", section.getTitle()),
                  tr.text(type, id, "subtitle", section.getSubtitle()),
                  tr.text(type, id, "body", section.getBody()),
                  tr.json(type, id, "content_json", section.getContentJson()),
                  section.getImageUrl(),
                  tr.text(type, id, "cta_label", section.getCtaLabel()),
                  section.getCtaUrl()
              )
          );
        });

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
        locale,
        toSalonDto(profile, tr),
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
                tr.text(ContentTranslator.GALLERY, item.getId(), "alt_text",
                    item.getAltText() != null ? item.getAltText() : item.getTitle()),
                tr.text(ContentTranslator.GALLERY, item.getId(), "caption", item.getCaption())
            ))
            .toList(),
        testimonialRepository.findByTenantIdAndPublishedTrueOrderByDisplayOrderAsc(tenantId)
            .stream()
            .map(item -> new TestimonialDto(
                tr.text(ContentTranslator.TESTIMONIAL, item.getId(), "content", item.getContent()),
                item.getAuthorName(),
                tr.text(ContentTranslator.TESTIMONIAL, item.getId(), "author_role", item.getAuthorRole()),
                item.getAvatarUrl()
            ))
            .toList(),
        sections,
        features
    );
  }

  /**
   * Returns the locale to overlay, or {@code null} for the tenant's default language.
   * A locale is applied only if the tenant lists it in its {@code localization} feature
   * and it differs from the tenant default (base rows already hold the default language).
   */
  @SuppressWarnings("unchecked")
  private static String resolveLocale(String requested, FeatureDto localization) {
    if (requested == null || requested.isBlank() || localization == null || !localization.enabled()) {
      return null;
    }
    String wanted = requested.trim().toLowerCase(Locale.ROOT);
    int dash = wanted.indexOf('-');
    String language = dash > 0 ? wanted.substring(0, dash) : wanted;

    Map<String, Object> config = localization.config();
    Object defaultLocale = config != null ? config.get("default") : null;
    Object offered = config != null ? config.get("locales") : null;
    if (!(offered instanceof List<?> list) || !list.contains(language)) {
      return null;
    }
    if (language.equalsIgnoreCase(String.valueOf(defaultLocale))) {
      return null;
    }
    return language;
  }

  private static SalonDto toSalonDto(SalonProfile profile, ContentTranslator tr) {
    String phone = profile.getPhone();
    String phoneHref = phone == null || phone.isBlank()
        ? null
        : (phone.startsWith("tel:") ? phone : "tel:" + phone);

    String address = joinAddress(profile.getAddressLine1(), profile.getState(), profile.getCity());

    UUID id = profile.getId();
    return new SalonDto(
        profile.getDisplayName(),
        tr.text(ContentTranslator.SALON_PROFILE, id, "tagline", profile.getTagline()),
        tr.text(ContentTranslator.SALON_PROFILE, id, "description", profile.getDescription()),
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

  private static ServiceDto toServiceDto(ServiceOffer service, ServicePrice price, ContentTranslator tr) {
    int amount = price != null ? price.getPriceCents() : 0;
    String currency = price != null ? price.getCurrency() : "RSD";
    UUID id = service.getId();
    return new ServiceDto(
        service.getCode(),
        service.getCode(),
        tr.text(ContentTranslator.SERVICE, id, "name", service.getName()),
        tr.text(ContentTranslator.SERVICE, id, "description", service.getDescription()),
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
