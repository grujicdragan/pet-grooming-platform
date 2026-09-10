package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.SalonBranding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonBrandingRepository extends JpaRepository<SalonBranding, UUID> {
  Optional<SalonBranding> findByTenantId(UUID tenantId);
}
