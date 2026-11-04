package br.com.fiap.veti.config;

import br.com.fiap.veti.handler.OAuth2SuccessHandler;
import br.com.fiap.veti.service.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.Set;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws
            Exception {
        // Login Google só é habilitado quando GOOGLE_CLIENT_ID está configurado (ver GoogleOAuth2Config)
        if (clientRegistrationRepository.getIfAvailable() != null) {
            httpSecurity.oauth2Login(oauth2 -> oauth2
                    .loginPage("/auth/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
            );
        }

        // Navegador (Accept: text/html) sem login vai para a tela de login; chamadas de API recebem 403
        var html = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        html.setIgnoredMediaTypes(Set.of(MediaType.ALL));

        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/auth/login"), html)
                        .defaultAuthenticationEntryPointFor(new Http403ForbiddenEntryPoint(), AnyRequestMatcher.INSTANCE))
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/error", "/css/**", "/img/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/logout", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health", "/health/**", "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasAuthority("ADMIN")
                        .requestMatchers(
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Gestão da equipe é exclusiva do ADMIN
                        .requestMatchers("/veterinarios/**", "/relatorios/**").hasAuthority("ADMIN")
                        // Agendar, notificar e cancelar consultas: qualquer veterinário logado (USER ou ADMIN)
                        .requestMatchers(HttpMethod.POST, "/consultas", "/consultas/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/consultas/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/consultas/**").authenticated()
                        // Cadastro, alteração e exclusão de tutores e pets: só ADMIN
                        .requestMatchers(HttpMethod.POST).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE).hasAuthority("ADMIN")
                        // Leitura (API e telas) exige login
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager
    authenticationManager(AuthenticationConfiguration
                                  authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
