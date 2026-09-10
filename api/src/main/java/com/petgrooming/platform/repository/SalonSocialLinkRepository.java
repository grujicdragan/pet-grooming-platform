package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.SalonSocialLink;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonSocialLinkRepository extends JpaRepository<SalonSocialLink, UUID> {
  List<SalonSocialLink> findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(UUID tenantId);
}
