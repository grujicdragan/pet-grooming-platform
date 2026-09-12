package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  Optional<Customer> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
