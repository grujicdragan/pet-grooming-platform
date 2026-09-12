package com.petgrooming.platform.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {

  private static final Logger log = LoggerFactory.getLogger(MailService.class);
  private static final HttpClient HTTP = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5))
      .build();

  private final String provider;
  private final String from;
  private final String resendApiKey;
  private final String webOrigin;
  private final List<String> allowedOrigins;

  public MailService(
      @Value("${app.mail.provider:log}") String provider,
      @Value("${app.mail.from}") String from,
      @Value("${app.mail.resend-api-key:}") String resendApiKey,
      @Value("${app.web-origin}") String webOrigin,
      @Value("${app.cors.allowed-origins}") String allowedOrigins
  ) {
    this.provider = provider;
    this.from = from;
    this.resendApiKey = resendApiKey == null ? "" : resendApiKey.trim();
    this.webOrigin = webOrigin.replaceAll("/$", "");
    this.allowedOrigins = List.of(allowedOrigins.split(",")).stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  public boolean sendsToInbox() {
    return "resend".equalsIgnoreCase(provider) && !resendApiKey.isBlank();
  }

  public String localVerificationLink(String rawToken, String requestOrigin) {
    if (sendsToInbox()) {
      return null;
    }
    return resolveOrigin(requestOrigin) + "/verify-email?token=" + rawToken;
  }

  public boolean sendVerification(String to, String name, String salonName, String rawToken, String requestOrigin) {
    String link = resolveOrigin(requestOrigin) + "/verify-email?token=" + rawToken;
    String subject = "Potvrdi nalog — " + salonName;
    String html = """
        <div style="font-family:Georgia,serif;line-height:1.5;color:#1c1917">
          <p>Zdravo %s,</p>
          <p>Potvrdi nalog za <strong>%s</strong> klikom na dugme:</p>
          <p><a href="%s" style="display:inline-block;padding:12px 18px;border-radius:999px;background:#3f5c4c;color:#fff;text-decoration:none">Potvrdi nalog</a></p>
          <p>Link važi 24 sata. Ako nisi ti kreirao nalog, ignoriši ovu poruku.</p>
        </div>
        """.formatted(escapeHtml(name), escapeHtml(salonName), link);

    try {
      if ("resend".equalsIgnoreCase(provider) && !resendApiKey.isBlank()) {
        return sendResend(to, subject, html);
      }
      log.info("Verification email for {} ({}) -> {}", to, salonName, link);
      return true;
    } catch (Exception ex) {
      log.error("Failed to send verification email to {}", to, ex);
      return false;
    }
  }

  private boolean sendResend(String to, String subject, String html) throws Exception {
    String body = """
        {"from":%s,"to":[%s],"subject":%s,"html":%s}
        """.formatted(json(from), json(to), json(subject), json(html));
    HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
        .timeout(Duration.ofSeconds(10))
        .header("Authorization", "Bearer " + resendApiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
    HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return true;
    }
    log.error("Resend rejected verification email: {} {}", response.statusCode(), response.body());
    return false;
  }

  private String resolveOrigin(String requestOrigin) {
    if (requestOrigin != null) {
      String origin = requestOrigin.trim().replaceAll("/$", "");
      if (allowedOrigins.contains(origin)) {
        return origin;
      }
    }
    return webOrigin;
  }

  private static String json(String value) {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "") + "\"";
  }

  private static String escapeHtml(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
