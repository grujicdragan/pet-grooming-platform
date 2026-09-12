package com.petgrooming.platform.dto;

public record AuthUserResponse(
    String id,
    String email,
    String name,
    String role
) {}
