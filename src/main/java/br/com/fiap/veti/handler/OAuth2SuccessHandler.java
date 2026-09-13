package br.com.fiap.veti.handler;

import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Autowired
    private TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        VeterinarioEntity veterinario = veterinarioRepository.findByEmail(email);
        String token = tokenService.generateToken(veterinario);

        Cookie cookie = new Cookie("veti_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        response.sendRedirect("/veti/consultas");
    }
}
