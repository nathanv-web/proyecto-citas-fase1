package com.proyectocitas.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.identity")
public class IdentityProperties {
    private Duration resetTokenTtl = Duration.ofMinutes(30);
    private Duration verificationTokenTtl = Duration.ofHours(24);
    private Duration requestWindow = Duration.ofMinutes(15);
    private URI frontendBaseUrl = URI.create("http://localhost:4200");
    @NotBlank private String mailFrom = "no-reply@citanova.local";
    @Min(1) private int emailRequestLimit = 3;
    @Min(1) private int ipRequestLimit = 20;
    @Min(2) private int maxRateLimitEntries = 10000;

    @AssertTrue(message = "Las duraciones de identidad deben ser positivas y no superar siete días")
    public boolean isDurationsValid() {
        return valid(resetTokenTtl) && valid(verificationTokenTtl) && valid(requestWindow);
    }

    @AssertTrue(message = "La URL del frontend debe ser HTTP/HTTPS absoluta, sin credenciales, query ni fragmento")
    public boolean isFrontendUrlValid() {
        return frontendBaseUrl != null && frontendBaseUrl.getHost() != null
                && ("http".equals(frontendBaseUrl.getScheme()) || "https".equals(frontendBaseUrl.getScheme()))
                && frontendBaseUrl.getUserInfo() == null && frontendBaseUrl.getQuery() == null
                && frontendBaseUrl.getFragment() == null;
    }

    private boolean valid(Duration value) {
        return value != null && !value.isNegative() && !value.isZero()
                && value.compareTo(Duration.ofDays(7)) <= 0;
    }

    public Duration getResetTokenTtl() { return resetTokenTtl; }
    public void setResetTokenTtl(Duration value) { resetTokenTtl = value; }
    public Duration getVerificationTokenTtl() { return verificationTokenTtl; }
    public void setVerificationTokenTtl(Duration value) { verificationTokenTtl = value; }
    public Duration getRequestWindow() { return requestWindow; }
    public void setRequestWindow(Duration value) { requestWindow = value; }
    public URI getFrontendBaseUrl() { return frontendBaseUrl; }
    public void setFrontendBaseUrl(URI value) { frontendBaseUrl = value; }
    public String getMailFrom() { return mailFrom; }
    public void setMailFrom(String value) { mailFrom = value; }
    public int getEmailRequestLimit() { return emailRequestLimit; }
    public void setEmailRequestLimit(int value) { emailRequestLimit = value; }
    public int getIpRequestLimit() { return ipRequestLimit; }
    public void setIpRequestLimit(int value) { ipRequestLimit = value; }
    public int getMaxRateLimitEntries() { return maxRateLimitEntries; }
    public void setMaxRateLimitEntries(int value) { maxRateLimitEntries = value; }
}
