package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.Testimonial;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonialRepository extends JpaRepository<Testimonial, UUID> {
  List<Testimonial> findByTenantIdAndPublishedTrueOrderByDisplayOrderAsc(UUID tenantId);
}
