package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.enms.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Disponibiliza o usuário logado para o layout das telas (menu, perfil e botões exclusivos do ADMIN).
 */
@ControllerAdvice(basePackages = "br.com.fiap.veti.controller.web")
public class WebModelAdvice {

    @ModelAttribute("usuario")
    public UsuarioLogado usuario(@AuthenticationPrincipal VeterinarioEntity veterinario) {
        if (veterinario == null) {
            return null;
        }
        return new UsuarioLogado(veterinario.getId(), veterinario.getEmail(),
                Role.ADMIN.name().equals(veterinario.getRole()));
    }

    public record UsuarioLogado(Long id, String email, boolean admin) {
    }
}
