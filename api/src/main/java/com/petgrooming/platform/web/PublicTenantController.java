package com.petgrooming.platform.web;

import com.petgrooming.platform.dto.TenantSiteResponse;
import com.petgrooming.platform.service.PublicTenantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/tenants")
public class PublicTenantController {

  private final PublicTenantService publicTenantService;

  public PublicTenantController(PublicTenantService publicTenantService) {
    this.publicTenantService = publicTenantService;
  }

  /**
   * @param locale optional BCP 47 language tag (e.g. {@code en}, {@code ru}). Content is
   *               overlaid with translations when the tenant offers that locale; otherwise
   *               the tenant's default language is returned.
   */
  @GetMapping("/{slug}")
  public TenantSiteResponse getTenantSite(
      @PathVariable String slug,
      @RequestParam(required = false) String locale
  ) {
    return publicTenantService.getSiteBySlug(slug, locale);
  }
}
