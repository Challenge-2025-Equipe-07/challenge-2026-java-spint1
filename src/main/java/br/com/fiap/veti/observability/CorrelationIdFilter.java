package br.com.fiap.veti.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Garante um correlation id por requisição: aceita o header X-Correlation-Id
 * (ou X-Request-Id), senão reutiliza o traceId do OpenTelemetry, senão gera um UUID.
 * O id vai para o MDC (logs JSON), para o header de resposta e para o span atual.
 * Também registra um log de acesso com nível INFO (2xx/3xx), WARN (4xx) ou ERROR (5xx).
 *
 * Roda logo depois do filtro de observação HTTP (HIGHEST_PRECEDENCE + 1) para já ter o trace ativo.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9._:-]{1,64}");

    private final ObjectProvider<Tracer> tracer;

    public CorrelationIdFilter(ObjectProvider<Tracer> tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Span span = currentSpan();
        String correlationId = resolveCorrelationId(request, span);
        long start = System.nanoTime();

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);
        if (span != null) {
            span.tag("correlation.id", correlationId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            logAccess(request, response, (System.nanoTime() - start) / 1_000_000);
            MDC.remove(MDC_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request, Span span) {
        for (String header : new String[]{HEADER, REQUEST_ID_HEADER}) {
            String value = request.getHeader(header);
            if (value != null && VALID_ID.matcher(value).matches()) {
                return value;
            }
        }
        if (span != null && span.context().traceId() != null && !span.context().traceId().isBlank()) {
            return span.context().traceId();
        }
        return UUID.randomUUID().toString();
    }

    private Span currentSpan() {
        Tracer current = tracer.getIfAvailable();
        return current != null ? current.currentSpan() : null;
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        int status = response.getStatus();
        String uri = request.getRequestURI();
        String message = "HTTP {} {} -> {} ({} ms)";
        if (status >= 500) {
            log.error(message, request.getMethod(), uri, status, durationMs);
        } else if (status >= 400) {
            log.warn(message, request.getMethod(), uri, status, durationMs);
        } else if (uri.contains("/actuator/health")) {
            log.debug(message, request.getMethod(), uri, status, durationMs);
        } else {
            log.info(message, request.getMethod(), uri, status, durationMs);
        }
    }
}
