package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.SiteSection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSectionRepository extends JpaRepository<SiteSection, UUID> {
  List<SiteSection> findByTenantIdAndVisibleTrueOrderByDisplayOrderAsc(UUID tenantId);
}
