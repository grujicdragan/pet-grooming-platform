package com.petgrooming.platform.dto;

public record RegisterResponse(String email, boolean emailSent, String verifyUrl) {}
