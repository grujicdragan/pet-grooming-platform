package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.Location;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, UUID> {

  @Query("""
      select l from Location l
      where l.tenantId = :tenantId and l.active = true
      order by l.primaryLocation desc, l.name asc
      """)
  List<Location> findActiveByTenantOrdered(@Param("tenantId") UUID tenantId);
}
