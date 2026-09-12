package com.petgrooming.platform.service;

import com.petgrooming.platform.security.AuthPrincipal;
import com.petgrooming.platform.web.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  public static final String CLAIM_EMAIL = "email";
  public static final String CLAIM_NAME = "name";
  public static final String CLAIM_TENANT_ID = "tenant_id";
  public static final String CLAIM_ROLE = "role";

  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder B64D = Base64.getUrlDecoder();
  private static final Pattern STRING_CLAIM = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
  private static final Pattern NUMBER_CLAIM = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(-?\\d+)");

  private final byte[] secret;
  private final Duration ttl;

  public JwtTokenService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.ttl}") Duration ttl
  ) {
    byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      throw new IllegalStateException("app.jwt.secret must be at least 32 bytes");
    }
    this.secret = bytes;
    this.ttl = ttl;
  }

  public String issue(UUID userId, String email, String name, UUID tenantId, String role) {
    Instant now = Instant.now();
    String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    String payload = compactJson(Map.of(
        "iss", "pet-grooming-api",
        "iat", now.getEpochSecond(),
        "exp", now.plus(ttl).getEpochSecond(),
        "sub", userId.toString(),
        CLAIM_EMAIL, email,
        CLAIM_NAME, name,
        CLAIM_TENANT_ID, tenantId.toString(),
        CLAIM_ROLE, role
    ));
    String signingInput = B64.encodeToString(header.getBytes(StandardCharsets.UTF_8))
        + "."
        + B64.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    return signingInput + "." + B64.encodeToString(hmac(signingInput));
  }

  public AuthPrincipal parse(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw invalidToken();
      }
      String signingInput = parts[0] + "." + parts[1];
      byte[] expected = hmac(signingInput);
      byte[] actual = B64D.decode(parts[2]);
      if (expected.length != actual.length || !constantTimeEquals(expected, actual)) {
        throw invalidToken();
      }

      String payload = new String(B64D.decode(parts[1]), StandardCharsets.UTF_8);
      Map<String, String> strings = stringClaims(payload);
      long exp = Long.parseLong(numberClaim(payload, "exp"));
      if (Instant.now().getEpochSecond() >= exp) {
        throw invalidToken();
      }

      return new AuthPrincipal(
          UUID.fromString(strings.get("sub")),
          UUID.fromString(strings.get(CLAIM_TENANT_ID)),
          strings.get(CLAIM_EMAIL),
          strings.get(CLAIM_NAME),
          strings.get(CLAIM_ROLE)
      );
    } catch (ApiException ex) {
      throw ex;
    } catch (Exception ex) {
      throw invalidToken();
    }
  }

  public long expiresInSeconds() {
    return ttl.toSeconds();
  }

  private static String compactJson(Map<String, Object> claims) {
    StringBuilder json = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, Object> entry : new LinkedHashMap<>(claims).entrySet()) {
      if (!first) {
        json.append(',');
      }
      first = false;
      json.append('"').append(entry.getKey()).append('"').append(':');
      Object value = entry.getValue();
      if (value instanceof Number number) {
        json.append(number);
      } else {
        json.append('"').append(escape(String.valueOf(value))).append('"');
      }
    }
    return json.append('}').toString();
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static Map<String, String> stringClaims(String json) {
    Map<String, String> claims = new LinkedHashMap<>();
    Matcher matcher = STRING_CLAIM.matcher(json);
    while (matcher.find()) {
      claims.put(matcher.group(1), matcher.group(2).replace("\\\"", "\"").replace("\\\\", "\\"));
    }
    return claims;
  }

  private static String numberClaim(String json, String name) {
    Matcher matcher = NUMBER_CLAIM.matcher(json);
    while (matcher.find()) {
      if (name.equals(matcher.group(1))) {
        return matcher.group(2);
      }
    }
    throw new IllegalArgumentException("Missing claim " + name);
  }

  private byte[] hmac(String signingInput) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
    } catch (Exception ex) {
      throw new IllegalStateException("Could not sign JWT", ex);
    }
  }

  private static boolean constantTimeEquals(byte[] a, byte[] b) {
    int diff = 0;
    for (int i = 0; i < a.length; i++) {
      diff |= a[i] ^ b[i];
    }
    return diff == 0;
  }

  private static ApiException invalidToken() {
    return ApiException.unauthorized("INVALID_TOKEN", "Invalid or expired token.");
  }
}
