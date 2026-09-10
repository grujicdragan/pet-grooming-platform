package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.ServiceOffer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, UUID> {
  List<ServiceOffer> findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(UUID tenantId);
}
