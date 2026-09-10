package com.petgrooming.platform.web;

import com.petgrooming.platform.dto.TenantSiteResponse;
import com.petgrooming.platform.service.PublicTenantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/tenants")
public class PublicTenantController {

  private final PublicTenantService publicTenantService;

  public PublicTenantController(PublicTenantService publicTenantService) {
    this.publicTenantService = publicTenantService;
  }

  @GetMapping("/{slug}")
  public TenantSiteResponse getTenantSite(@PathVariable String slug) {
    return publicTenantService.getSiteBySlug(slug);
  }
}
