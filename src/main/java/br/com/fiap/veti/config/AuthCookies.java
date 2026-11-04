package br.com.fiap.veti.config;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Cookie que guarda o JWT para as telas web. HttpOnly + SameSite=Lax impedem leitura via JS
 * e envio em POSTs de outros sites (a API é stateless e não usa token CSRF).
 */
public final class AuthCookies {

    public static final String NAME = "veti_token";

    private AuthCookies() {
    }

    public static String create(String token, boolean secure) {
        return build(token, Duration.ofHours(1), secure);
    }

    public static String clear(boolean secure) {
        return build("", Duration.ZERO, secure);
    }

    private static String build(String value, Duration maxAge, boolean secure) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString();
    }
}
