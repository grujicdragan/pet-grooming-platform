package com.petgrooming.platform.repository;

import com.petgrooming.platform.domain.EmailVerificationToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
  Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

  @Modifying(clearAutomatically = true)
  @Query("delete from EmailVerificationToken t where t.userId = :userId and t.consumedAt is null")
  void deleteOpenTokens(@Param("userId") UUID userId);
}
