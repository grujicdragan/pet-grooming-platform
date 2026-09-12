package com.petgrooming.platform.security;

import java.util.UUID;

public record AuthPrincipal(
    UUID userId,
    UUID tenantId,
    String email,
    String name,
    String role
) {
  public static final String ATTR = "auth.principal";
}
