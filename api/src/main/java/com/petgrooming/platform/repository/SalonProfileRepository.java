package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.SalonProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonProfileRepository extends JpaRepository<SalonProfile, UUID> {
  Optional<SalonProfile> findByTenantId(UUID tenantId);
}
