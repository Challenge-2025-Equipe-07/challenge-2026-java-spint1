package br.com.fiap.veti.config;

import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.enms.Role;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.service.VeterinarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro ADMIN a partir de ADMIN_EMAIL/ADMIN_PASSWORD, já que o auto-cadastro público só gera USER.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioService veterinarioService;

    @Value("${veti.admin.email:}")
    private String email;

    @Value("${veti.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD nao definidos; nenhum administrador inicial criado");
            return;
        }
        if (veterinarioRepository.findByEmail(email) != null) {
            return;
        }
        veterinarioService.cadastrar(new RegisterRequest(email, password, Role.ADMIN), Role.ADMIN);
        log.info("Administrador inicial criado");
    }
}
