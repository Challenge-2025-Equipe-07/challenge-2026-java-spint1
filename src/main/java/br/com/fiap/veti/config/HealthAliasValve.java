package br.com.fiap.veti.config;

import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import java.io.IOException;

/**
 * Valve no nível do Engine do Tomcat que expõe GET /health fora do context-path (/veti).
 * Requisições para /health (sem contexto mapeado) são remapeadas internamente para o
 * endpoint do Actuator (/veti/actuator/health), sem redirect.
 */
public class HealthAliasValve extends ValveBase {

    private static final String ALIAS = "/health";

    private final String target;

    public HealthAliasValve(String target) {
        super(true);
        this.target = target;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        String uri = request.getDecodedRequestURI();
        if (request.getContext() == null && uri != null
                && (uri.equals(ALIAS) || uri.startsWith(ALIAS + "/"))) {
            String newUri = target + uri.substring(ALIAS.length());
            var coyoteRequest = request.getCoyoteRequest();
            coyoteRequest.requestURI().setString(newUri);
            coyoteRequest.decodedURI().setString(newUri);
            request.getMappingData().recycle();
            request.getConnector().getService().getMapper()
                    .map(coyoteRequest.serverName(), coyoteRequest.decodedURI(), null, request.getMappingData());
        }
        getNext().invoke(request, response);
    }
}
