package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.dto.response.VeterinarioResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.enms.Role;
import br.com.fiap.veti.mapper.VeterinarioMapper;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.repository.procedure.VetiPackageRepository;
import br.com.fiap.veti.service.util.EntityFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private static final String SENHA_VALIDA = "SENHA_VALIDA";

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final VetiPackageRepository vetiPackageRepository;

    @Cacheable("veterinarios")
    public Page<VeterinarioResponse> listarTodos(Pageable pageable) {
        return veterinarioRepository.findAll(pageable).map(entity -> mapper.toResponse(entity));
    }

    @Cacheable(value = "veterinario", key = "#id")
    public VeterinarioResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(value = {"veterinarios", "veterinario"}, allEntries = true)
    public VeterinarioResponse cadastrar(RegisterRequest request, Role role) {
        if (veterinarioRepository.findByEmail(request.email()) != null) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        // Regra de senha centralizada no banco: PKG_VETI.FN_VALIDA_SENHA
        String validacao = vetiPackageRepository.validarSenha(request.password());
        if (!SENHA_VALIDA.equals(validacao)) {
            throw new IllegalArgumentException(validacao.replaceFirst("^ERRO:\\s*", ""));
        }
        VeterinarioEntity veterinario = mapper.toEntity(request);
        veterinario.setPassword(passwordEncoder.encode(request.password()));
        veterinario.setRole(role.name());
        VeterinarioEntity salvo = veterinarioRepository.save(veterinario);
        log.info("Veterinario cadastrado id={} role={}", salvo.getId(), role);
        return mapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = {"veterinarios", "veterinario"}, allEntries = true)
    public void deletar(Long id) {
        veterinarioRepository.delete(buscarEntidade(id));
    }

    public VeterinarioEntity buscarEntidade(Long id) {
        return EntityFinder.findOrThrow(veterinarioRepository, id, "Veterinário");
    }
}
