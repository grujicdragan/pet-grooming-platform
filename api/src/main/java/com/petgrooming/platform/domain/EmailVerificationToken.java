package com.petgrooming.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

  @Id
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }

  public static EmailVerificationToken issue(UUID userId, String tokenHash, Instant expiresAt) {
    EmailVerificationToken token = new EmailVerificationToken();
    token.userId = userId;
    token.tokenHash = tokenHash;
    token.expiresAt = expiresAt;
    return token;
  }

  public boolean isUsable(Instant now) {
    return consumedAt == null && expiresAt.isAfter(now);
  }

  public void consume(Instant at) {
    this.consumedAt = at;
  }

  public UUID getUserId() {
    return userId;
  }
}
