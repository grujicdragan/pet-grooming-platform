package com.petgrooming.platform.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    AuthUserResponse user
) {}
