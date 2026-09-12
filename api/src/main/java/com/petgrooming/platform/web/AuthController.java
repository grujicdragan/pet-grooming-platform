package com.petgrooming.platform.web;

import com.petgrooming.platform.dto.AuthResponse;
import com.petgrooming.platform.dto.AuthUserResponse;
import com.petgrooming.platform.dto.LoginRequest;
import com.petgrooming.platform.dto.RegisterRequest;
import com.petgrooming.platform.dto.RegisterResponse;
import com.petgrooming.platform.dto.ResendVerificationRequest;
import com.petgrooming.platform.dto.VerifyEmailRequest;
import com.petgrooming.platform.security.AuthPrincipal;
import com.petgrooming.platform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  static final String TENANT_HEADER = "X-Tenant-Slug";

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public RegisterResponse register(
      @RequestHeader(value = TENANT_HEADER, required = false) String tenantSlug,
      @RequestHeader(value = "Origin", required = false) String origin,
      @Valid @RequestBody RegisterRequest request
  ) {
    return authService.register(tenantSlug, request, origin);
  }

  @PostMapping("/login")
  public AuthResponse login(
      @RequestHeader(value = TENANT_HEADER, required = false) String tenantSlug,
      @Valid @RequestBody LoginRequest request
  ) {
    return authService.login(tenantSlug, request);
  }

  @PostMapping("/verify-email")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    authService.verifyEmail(request.token());
  }

  @PostMapping("/resend-verification")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resend(
      @RequestHeader(value = TENANT_HEADER, required = false) String tenantSlug,
      @RequestHeader(value = "Origin", required = false) String origin,
      @Valid @RequestBody ResendVerificationRequest request
  ) {
    authService.resendVerification(tenantSlug, request.email(), origin);
  }

  @GetMapping("/me")
  public AuthUserResponse me(
      @RequestHeader(value = TENANT_HEADER, required = false) String tenantSlug,
      HttpServletRequest request
  ) {
    AuthPrincipal principal = (AuthPrincipal) request.getAttribute(AuthPrincipal.ATTR);
    return authService.me(tenantSlug, principal.userId(), principal.tenantId());
  }
}
