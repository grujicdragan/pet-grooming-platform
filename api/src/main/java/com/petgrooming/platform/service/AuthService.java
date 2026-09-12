package com.petgrooming.platform.service;

import com.petgrooming.platform.config.RlsSessionSupport;
import com.petgrooming.platform.domain.Customer;
import com.petgrooming.platform.domain.EmailVerificationToken;
import com.petgrooming.platform.domain.Tenant;
import com.petgrooming.platform.domain.TenantUser;
import com.petgrooming.platform.domain.UserAccount;
import com.petgrooming.platform.domain.UserRole;
import com.petgrooming.platform.dto.AuthResponse;
import com.petgrooming.platform.dto.AuthUserResponse;
import com.petgrooming.platform.dto.LoginRequest;
import com.petgrooming.platform.dto.RegisterRequest;
import com.petgrooming.platform.dto.RegisterResponse;
import com.petgrooming.platform.repository.CustomerRepository;
import com.petgrooming.platform.repository.EmailVerificationTokenRepository;
import com.petgrooming.platform.repository.TenantRepository;
import com.petgrooming.platform.repository.TenantUserRepository;
import com.petgrooming.platform.repository.UserAccountRepository;
import com.petgrooming.platform.security.PasswordHasher;
import com.petgrooming.platform.security.TokenHasher;
import com.petgrooming.platform.web.ApiException;
import com.petgrooming.platform.web.NotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AuthService {

  private static final Duration VERIFY_TTL = Duration.ofHours(24);

  private final RlsSessionSupport rls;
  private final TenantRepository tenantRepository;
  private final UserAccountRepository users;
  private final TenantUserRepository tenantUsers;
  private final CustomerRepository customers;
  private final EmailVerificationTokenRepository verificationTokens;
  private final PasswordHasher passwordHasher;
  private final JwtTokenService jwtTokenService;
  private final MailService mailService;

  public AuthService(
      RlsSessionSupport rls,
      TenantRepository tenantRepository,
      UserAccountRepository users,
      TenantUserRepository tenantUsers,
      CustomerRepository customers,
      EmailVerificationTokenRepository verificationTokens,
      PasswordHasher passwordHasher,
      JwtTokenService jwtTokenService,
      MailService mailService
  ) {
    this.rls = rls;
    this.tenantRepository = tenantRepository;
    this.users = users;
    this.tenantUsers = tenantUsers;
    this.customers = customers;
    this.verificationTokens = verificationTokens;
    this.passwordHasher = passwordHasher;
    this.jwtTokenService = jwtTokenService;
    this.mailService = mailService;
  }

  @Transactional
  public RegisterResponse register(String tenantSlug, RegisterRequest request, String origin) {
    Tenant tenant = requireTenant(tenantSlug);
    String email = normalizeEmail(request.email());
    var existing = users.findByEmailIgnoreCase(email);
    if (existing.isPresent()) {
      UserAccount user = existing.get();
      if (user.isEmailVerified()) {
        throw ApiException.conflict("EMAIL_TAKEN", "An account with this email already exists.");
      }
      ensureSalonMembership(tenant.getId(), user);
      VerificationMail mail = issueAndSendVerification(user, tenant, origin);
      return new RegisterResponse(user.getEmail(), mail.sent(), mail.verifyUrl());
    }

    NameParts name = splitName(request.name());
    UserAccount user = users.saveAndFlush(UserAccount.create(
        email,
        passwordHasher.hash(request.password()),
        name.first(),
        name.last()
    ));
    ensureSalonMembership(tenant.getId(), user);
    VerificationMail mail = issueAndSendVerification(user, tenant, origin);
    return new RegisterResponse(user.getEmail(), mail.sent(), mail.verifyUrl());
  }

  @Transactional
  public AuthResponse login(String tenantSlug, LoginRequest request) {
    Tenant tenant = requireTenant(tenantSlug);
    String email = normalizeEmail(request.email());
    UserAccount user = users.findByEmailIgnoreCase(email)
        .orElseThrow(AuthService::invalidCredentials);

    if (!user.isActive() || user.getPasswordHash() == null
        || !passwordHasher.matches(request.password(), user.getPasswordHash())) {
      throw invalidCredentials();
    }
    if (!user.isEmailVerified()) {
      throw ApiException.forbidden("EMAIL_NOT_VERIFIED", "Confirm your email before signing in.");
    }

    TenantUser membership = ensureSalonMembership(tenant.getId(), user);
    if (!membership.isActive()) {
      throw ApiException.unauthorized("ACCOUNT_DISABLED", "This account is disabled.");
    }

    user.markLoggedIn(Instant.now());
    return issue(user, tenant, membership.getRole());
  }

  @Transactional
  public void verifyEmail(String token) {
    if (token == null || token.isBlank()) {
      throw ApiException.badRequest("INVALID_TOKEN", "Missing verification token.");
    }
    EmailVerificationToken row = verificationTokens.findByTokenHash(TokenHasher.sha256(token.trim()))
        .orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "Invalid or expired token."));
    Instant now = Instant.now();
    if (!row.isUsable(now)) {
      throw ApiException.badRequest("INVALID_TOKEN", "Invalid or expired token.");
    }
    UserAccount user = users.findById(row.getUserId())
        .orElseThrow(() -> ApiException.badRequest("INVALID_TOKEN", "Invalid or expired token."));
    user.markEmailVerified(now);
    row.consume(now);
  }

  @Transactional
  public void resendVerification(String tenantSlug, String email, String origin) {
    Tenant tenant = requireTenant(tenantSlug);
    users.findByEmailIgnoreCase(normalizeEmail(email))
        .filter(user -> user.isActive() && !user.isEmailVerified())
        .ifPresent(user -> issueAndSendVerification(user, tenant, origin));
  }

  @Transactional(readOnly = true)
  public AuthUserResponse me(String tenantSlug, UUID userId, UUID tokenTenantId) {
    Tenant tenant = requireTenant(tenantSlug);
    if (!tenant.getId().equals(tokenTenantId)) {
      throw ApiException.unauthorized("INVALID_TOKEN", "Token does not match this salon.");
    }
    UserAccount user = users.findById(userId)
        .filter(UserAccount::isActive)
        .orElseThrow(() -> ApiException.unauthorized("INVALID_TOKEN", "Account not found."));
    TenantUser membership = tenantUsers.findByTenantIdAndUserId(tenant.getId(), userId)
        .filter(TenantUser::isActive)
        .orElseThrow(() -> ApiException.unauthorized("INVALID_TOKEN", "No membership for this salon."));
    return toUserResponse(user, membership.getRole());
  }

  private VerificationMail issueAndSendVerification(UserAccount user, Tenant tenant, String origin) {
    users.flush();
    verificationTokens.deleteOpenTokens(user.getId());
    String raw = TokenHasher.randomToken();
    verificationTokens.save(EmailVerificationToken.issue(
        user.getId(),
        TokenHasher.sha256(raw),
        Instant.now().plus(VERIFY_TTL)
    ));
    String name = user.displayName();
    String salon = tenant.getName();
    String to = user.getEmail();
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          mailService.sendVerification(to, name, salon, raw, origin);
        }
      });
      return new VerificationMail(true, mailService.localVerificationLink(raw, origin));
    }
    boolean sent = mailService.sendVerification(to, name, salon, raw, origin);
    return new VerificationMail(sent, mailService.localVerificationLink(raw, origin));
  }

  private TenantUser ensureSalonMembership(UUID tenantId, UserAccount user) {
    TenantUser membership = tenantUsers.findByTenantIdAndUserId(tenantId, user.getId())
        .orElseGet(() -> tenantUsers.save(TenantUser.membership(tenantId, user.getId(), UserRole.CUSTOMER)));
    customers.findByTenantIdAndUserId(tenantId, user.getId())
        .orElseGet(() -> customers.save(Customer.forUser(tenantId, user)));
    return membership;
  }

  private AuthResponse issue(UserAccount user, Tenant tenant, UserRole role) {
    String token = jwtTokenService.issue(
        user.getId(),
        user.getEmail(),
        user.displayName(),
        tenant.getId(),
        role.name()
    );
    return new AuthResponse(
        token,
        "Bearer",
        jwtTokenService.expiresInSeconds(),
        toUserResponse(user, role)
    );
  }

  private static AuthUserResponse toUserResponse(UserAccount user, UserRole role) {
    return new AuthUserResponse(
        user.getId().toString(),
        user.getEmail(),
        user.displayName(),
        role.name()
    );
  }

  private Tenant requireTenant(String tenantSlug) {
    if (tenantSlug == null || tenantSlug.isBlank()) {
      throw ApiException.badRequest("TENANT_REQUIRED", "X-Tenant-Slug header is required.");
    }
    rls.enableBypass();
    Tenant tenant = tenantRepository.findBySlugIgnoreCaseAndActiveTrue(tenantSlug.trim())
        .orElseThrow(() -> new NotFoundException("Unknown tenant"));
    rls.setCurrentTenant(tenant.getId());
    return tenant;
  }

  private static String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private static NameParts splitName(String raw) {
    String trimmed = raw.trim().replaceAll("\\s+", " ");
    int space = trimmed.indexOf(' ');
    if (space < 0) {
      return new NameParts(trimmed, "");
    }
    return new NameParts(trimmed.substring(0, space), trimmed.substring(space + 1));
  }

  private static ApiException invalidCredentials() {
    return ApiException.unauthorized("INVALID_CREDENTIALS", "Email or password is incorrect.");
  }

  private record NameParts(String first, String last) {}

  private record VerificationMail(boolean sent, String verifyUrl) {}
}
