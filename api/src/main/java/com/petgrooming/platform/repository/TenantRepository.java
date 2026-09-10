package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
  Optional<Tenant> findBySlugIgnoreCaseAndActiveTrue(String slug);
}
