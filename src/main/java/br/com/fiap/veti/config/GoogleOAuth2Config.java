package br.com.fiap.veti.config;

import br.com.fiap.veti.observability.GoogleOAuthHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Login com Google é opcional: só é registrado quando GOOGLE_CLIENT_ID está definido.
 * Sem ele a aplicação sobe normalmente apenas com login JWT.
 */
@Configuration
@ConditionalOnExpression("!'${GOOGLE_CLIENT_ID:}'.isBlank()")
public class GoogleOAuth2Config {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${GOOGLE_CLIENT_ID}") String clientId,
            @Value("${GOOGLE_CLIENT_SECRET:}") String clientSecret) {
        return new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("email", "profile")
                .build());
    }

    @Bean
    public GoogleOAuthHealthIndicator googleOAuthHealthIndicator() {
        return new GoogleOAuthHealthIndicator();
    }
}
