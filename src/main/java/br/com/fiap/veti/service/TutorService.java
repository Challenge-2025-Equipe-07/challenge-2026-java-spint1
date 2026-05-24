package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.TutorRequest;
import br.com.fiap.veti.dto.response.TutorResponse;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.mapper.TutorMapper;
import br.com.fiap.veti.repository.TutorRepository;
import br.com.fiap.veti.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorService {

    @Autowired
    TutorRepository tutorRepository;

    @Autowired
    TutorMapper mapper;
    @Autowired
    private VeterinarioRepository veterinarioRepository;

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
        return mapper.toResponse(tutorRepository.save(tutor));
    }

    private void verifyCpf(TutorRequest request) {
        tutorRepository.findByCpf(request.cpf()).ifPresent(t -> {
            throw new IllegalArgumentException("CPF já cadastrado");
        });
    }

    private void verifyEmail(TutorRequest request) {
        tutorRepository.findByEmail(request.email()).ifPresent(t -> {
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
        return mapper.toResponse(tutorRepository.save(tutor));
    }

    @Transactional
    @CacheEvict(value = {"tutores", "tutor"}, allEntries = true)
    public void deletar(Long id) {
        tutorRepository.delete(buscarEntidade(id));
    }

    private TutorEntity buscarEntidade(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TutorEntity não encontrado: " + id));
    }
}