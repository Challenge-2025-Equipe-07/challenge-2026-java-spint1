package br.com.fiap.veti.observability;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Verifica a dependência externa do login Google (documento OpenID Connect).
 * Falha reporta DEGRADED (e não DOWN) para não derrubar a instância no App Service
 * quando apenas o provedor externo está indisponível.
 */
public class GoogleOAuthHealthIndicator implements HealthIndicator {

    public static final Status DEGRADED = new Status("DEGRADED");
    private static final URI DISCOVERY = URI.create("https://accounts.google.com/.well-known/openid-configuration");
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    @Override
    public Health health() {
        try {
            HttpRequest request = HttpRequest.newBuilder(DISCOVERY).timeout(TIMEOUT).GET().build();
            int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            Health.Builder builder = status == 200 ? Health.up() : Health.status(DEGRADED);
            return builder.withDetail("endpoint", DISCOVERY.toString()).withDetail("httpStatus", status).build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Health.status(DEGRADED).withDetail("endpoint", DISCOVERY.toString()).withException(e).build();
        } catch (Exception e) {
            return Health.status(DEGRADED).withDetail("endpoint", DISCOVERY.toString()).withException(e).build();
        }
    }
}
