package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.TenantUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantUserRepository extends JpaRepository<TenantUser, UUID> {
  Optional<TenantUser> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
