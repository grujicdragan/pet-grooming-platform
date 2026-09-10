package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.TenantFeature;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantFeatureRepository extends JpaRepository<TenantFeature, UUID> {
  List<TenantFeature> findByTenantId(UUID tenantId);
}
