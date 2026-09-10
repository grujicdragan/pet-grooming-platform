package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.SalonGalleryItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonGalleryRepository extends JpaRepository<SalonGalleryItem, UUID> {
  List<SalonGalleryItem> findByTenantIdAndPublishedTrueOrderByDisplayOrderAsc(UUID tenantId);
}
