package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.PetRequest;
import br.com.fiap.veti.dto.response.PetResponse;
import br.com.fiap.veti.entity.PetEntity;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.mapper.PetMapper;
import br.com.fiap.veti.repository.PetRepository;
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
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final PetMapper mapper;


    @Cacheable("pets")
    public Page<PetResponse> listarTodos(Pageable pageable) {
        return petRepository.findAll(pageable).map(entity -> mapper.toResponse(entity));
    }

    @Cacheable(value = "pet", key = "#id")
    public PetResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    public PetResponse criar(PetRequest request) {
        TutorEntity tutor = EntityFinder.findOrThrow(tutorRepository, request.idTutor(), "Tutor");
        PetEntity pet = PetEntity.builder()
                .nome(request.name())
                .raca(request.raca().name())
                .peso(request.peso())
                .idade(request.idade())
                .castrado(Boolean.TRUE.equals(request.castrado()))
                .tutor(tutor)
                .build();
        PetEntity salvo = petRepository.save(pet);
        log.info("Pet criado id={} tutorId={}", salvo.getId(), tutor.getId());
        return mapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    public PetResponse atualizar(Long id, PetRequest request) {
        PetEntity pet = buscarEntidade(id);
        TutorEntity tutor = EntityFinder.findOrThrow(tutorRepository, request.idTutor(), "Tutor");
        pet.setNome(request.name());
        pet.setRaca(request.raca().name());
        pet.setPeso(request.peso());
        pet.setIdade(request.idade());
        pet.setCastrado(Boolean.TRUE.equals(request.castrado()));
        pet.setTutor(tutor);
        PetEntity salvo = petRepository.save(pet);
        log.info("Pet atualizado id={}", id);
        return mapper.toResponse(salvo);
    }

    @Transactional
    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    public void deletar(Long id) {
        petRepository.delete(buscarEntidade(id));
        log.info("Pet removido id={}", id);
    }

    private PetEntity buscarEntidade(Long id) {
        return EntityFinder.findOrThrow(petRepository, id, "Pet");
    }

}
