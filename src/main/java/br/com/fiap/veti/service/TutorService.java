package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.TutorRequest;
import br.com.fiap.veti.dto.response.TutorResponse;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.mapper.TutorMapper;
import br.com.fiap.veti.repository.TutorRepository;
import br.com.fiap.veti.service.util.EntityFinder;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed(name = "veti.service")
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;
    private final TutorMapper mapper;

    @Cacheable("tutores")
    public Page<TutorResponse> listarTodos(Pageable pageable) {
        return tutorRepository.findAll(pageable).map(entity -> mapper.toResponse(entity));
    }

    @Cacheable(value = "tutor", key = "#id")
    public TutorResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(value = {"tutores", "tutor"}, allEntries = true)
    public TutorResponse criar(TutorRequest request) {
        verifyCpf(request);
        verifyEmail(request);
        TutorEntity tutor = TutorEntity.builder()
                .nome(request.name())
                .cpf(request.cpf())
                .email(request.email())
                .telefone(request.phone())
                .build();
        TutorEntity salvo = tutorRepository.save(tutor);
        log.info("Tutor criado id={}", salvo.getId());
        return mapper.toResponse(salvo);
    }

    private void verifyCpf(TutorRequest request) {
        tutorRepository.findByCpf(request.cpf()).ifPresent(t -> {
            log.warn("Tentativa de cadastro com CPF duplicado");
            throw new IllegalArgumentException("CPF já cadastrado");
        });
    }

    private void verifyEmail(TutorRequest request) {
        tutorRepository.findByEmail(request.email()).ifPresent(t -> {
            log.warn("Tentativa de cadastro com e-mail duplicado");
            throw new IllegalArgumentException("E-mail já cadastrado");
        });
    }

    @Transactional
    @CacheEvict(value = {"tutores", "tutor"}, allEntries = true)
    public TutorResponse atualizar(Long id, TutorRequest request) {
        TutorEntity tutor = buscarEntidade(id);
        tutor.setNome(request.name());
        tutor.setCpf(request.cpf());
        tutor.setEmail(request.email());
        tutor.setTelefone(request.phone());
        TutorEntity salvo = tutorRepository.save(tutor);
        log.info("Tutor atualizado id={}", id);
        return mapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = {"tutores", "tutor"}, allEntries = true)
    public void deletar(Long id) {
        tutorRepository.delete(buscarEntidade(id));
        log.info("Tutor removido id={}", id);
    }

    private TutorEntity buscarEntidade(Long id) {
        return EntityFinder.findOrThrow(tutorRepository, id, "Tutor");
    }
}