package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.response.VeterinarioResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.mapper.VeterinarioMapper;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.service.support.EntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper mapper;

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
    public void deletar(Long id) {
        veterinarioRepository.delete(buscarEntidade(id));
    }

    public VeterinarioEntity buscarEntidade(Long id) {
        return EntityFinder.findOrThrow(veterinarioRepository, id, "Veterinário");
    }
}