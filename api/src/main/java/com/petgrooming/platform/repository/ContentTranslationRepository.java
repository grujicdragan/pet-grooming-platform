package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.ContentTranslation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentTranslationRepository extends JpaRepository<ContentTranslation, UUID> {

  List<ContentTranslation> findByTenantIdAndLocale(UUID tenantId, String locale);
}
