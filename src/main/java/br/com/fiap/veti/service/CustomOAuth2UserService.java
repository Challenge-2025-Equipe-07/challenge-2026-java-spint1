package br.com.fiap.veti.service;

import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import br.com.fiap.veti.enms.Role;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final VeterinarioRepository veterinarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        VeterinarioEntity veterinario = veterinarioRepository.findByEmail(email);

        if (veterinario == null) {
            veterinario = new VeterinarioEntity();
            veterinario.setEmail(email);
            veterinario.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            veterinario.setRole(Role.ADMIN.name());
            veterinarioRepository.save(veterinario);
        }

        return oAuth2User;
    }
}