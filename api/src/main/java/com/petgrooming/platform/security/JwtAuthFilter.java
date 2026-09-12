package com.petgrooming.platform.security;

import com.petgrooming.platform.dto.ErrorResponse;
import com.petgrooming.platform.service.JwtTokenService;
import com.petgrooming.platform.web.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtTokenService jwtTokenService;

  public JwtAuthFilter(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublic(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        AuthPrincipal principal = jwtTokenService.parse(header.substring(7).trim());
        request.setAttribute(AuthPrincipal.ATTR, principal);
      } catch (ApiException ex) {
        writeError(response, ex);
        return;
      }
    }

    if (request.getAttribute(AuthPrincipal.ATTR) == null) {
      writeError(response, ApiException.unauthorized("UNAUTHORIZED", "Authentication required."));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private static boolean isPublic(HttpServletRequest request) {
    String path = request.getRequestURI();
    String method = request.getMethod();
    if ("/api/health".equals(path) || path.startsWith("/api/public/")) {
      return true;
    }
    return "POST".equals(method)
        && ("/api/auth/login".equals(path)
            || "/api/auth/register".equals(path)
            || "/api/auth/verify-email".equals(path)
            || "/api/auth/resend-verification".equals(path));
  }

  private static void writeError(HttpServletResponse response, ApiException ex) throws IOException {
    response.setStatus(ex.getStatus().value());
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ErrorResponse body = new ErrorResponse(ex.getCode(), ex.getMessage());
    String json = "{\"code\":\"" + escape(body.code()) + "\",\"message\":\"" + escape(body.message()) + "\"}";
    response.getWriter().write(json);
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
