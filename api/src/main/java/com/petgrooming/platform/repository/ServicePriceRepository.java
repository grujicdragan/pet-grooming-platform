package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.ServicePrice;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePriceRepository extends JpaRepository<ServicePrice, UUID> {
  List<ServicePrice> findByTenantId(UUID tenantId);
}
