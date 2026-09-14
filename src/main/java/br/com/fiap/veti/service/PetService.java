package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.PetRequest;
import br.com.fiap.veti.dto.response.PetResponse;
import br.com.fiap.veti.entity.PetEntity;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.mapper.PetMapper;
import br.com.fiap.veti.repository.PetRepository;
import br.com.fiap.veti.repository.TutorRepository;
import br.com.fiap.veti.service.util.EntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
                .castrado(request.castrado())
                .tutor(tutor)
                .build();
        return mapper.toResponse(petRepository.save(pet));
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
        pet.setCastrado(request.castrado());
        pet.setTutor(tutor);
        return mapper.toResponse(petRepository.save(pet));
    }

    @Transactional
    @CacheEvict(value = {"pets", "pet"}, allEntries = true)
    public void deletar(Long id) {
        petRepository.delete(buscarEntidade(id));
    }

    private PetEntity buscarEntidade(Long id) {
        return EntityFinder.findOrThrow(petRepository, id, "Pet");
    }

}
