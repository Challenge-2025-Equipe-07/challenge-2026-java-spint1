package br.com.fiap.veti.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthEndpointConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> healthAliasCustomizer(
            @Value("${server.servlet.context-path:}") String contextPath,
            @Value("${management.endpoints.web.base-path:/actuator}") String actuatorBasePath) {
        return factory -> factory.addEngineValves(new HealthAliasValve(contextPath + actuatorBasePath + "/health"));
    }
}
