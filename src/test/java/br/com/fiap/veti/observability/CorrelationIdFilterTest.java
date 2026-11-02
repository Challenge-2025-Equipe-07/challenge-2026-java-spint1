package br.com.fiap.veti.observability;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CorrelationIdFilterTest {

    @SuppressWarnings("unchecked")
    private final CorrelationIdFilter filter = new CorrelationIdFilter(mock(ObjectProvider.class));

    @Test
    void devePropagarCorrelationIdRecebidoNoHeader() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/veti/tutores");
        request.addHeader(CorrelationIdFilter.HEADER, "abc-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcDuranteRequest = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuranteRequest.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(mdcDuranteRequest.get()).isEqualTo("abc-123");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("abc-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void deveGerarCorrelationIdQuandoHeaderInvalido() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/veti/pets");
        request.addHeader(CorrelationIdFilter.HEADER, "valor invalido com espacos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, (req, res) -> { });

        // Assert
        assertThat(response.getHeader(CorrelationIdFilter.HEADER))
                .isNotEqualTo("valor invalido com espacos")
                .matches("[0-9a-f-]{36}");
    }
}
