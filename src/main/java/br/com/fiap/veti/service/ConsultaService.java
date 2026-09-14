package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.ConsultaRequest;
import br.com.fiap.veti.dto.response.ConsultaResponse;
import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.entity.PetEntity;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.mapper.ConsultaMapper;
import br.com.fiap.veti.repository.ConsultaRepository;
import br.com.fiap.veti.repository.PetRepository;
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
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ConsultaMapper mapper;

    @Cacheable("consultas")
    public Page<ConsultaResponse> listarTodos(Pageable pageable) {
        return consultaRepository.findAll(pageable).map(entity -> mapper.toResponse(entity));
    }

    @Cacheable(value = "consulta", key = "#id")
    public ConsultaResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(value = {"consultas", "consulta"}, allEntries = true)
    public ConsultaResponse criar(ConsultaRequest request) {
        PetEntity pet = EntityFinder.findOrThrow(petRepository, request.idPet(), "Pet");
        VeterinarioEntity vet = EntityFinder.findOrThrow(veterinarioRepository, request.idVeterinario(), "Veterinário");

        boolean jaExiste = consultaRepository.findByVeterinarioId(request.idVeterinario(), Pageable.unpaged())
                .stream()
                .anyMatch(c -> c.getDataConsulta().equals(request.dataConsulta()));
        if (jaExiste) {
            throw new IllegalArgumentException("Veterinário já possui consulta nessa data");
        }

        ConsultaEntity consulta = ConsultaEntity.builder()
                .dataConsulta(request.dataConsulta())
                .pet(pet)
                .veterinario(vet)
                .build();
        return mapper.toResponse(consultaRepository.save(consulta));
    }

    @Transactional
    @CacheEvict(value = {"consultas", "consulta"}, allEntries = true)
    public ConsultaResponse atualizar(Long id, ConsultaRequest request) {
        ConsultaEntity consulta = buscarEntidade(id);
        PetEntity pet = EntityFinder.findOrThrow(petRepository, request.idPet(), "Pet");
        VeterinarioEntity vet = EntityFinder.findOrThrow(veterinarioRepository, request.idVeterinario(), "Veterinário");
        consulta.setDataConsulta(request.dataConsulta());
        consulta.setPet(pet);
        consulta.setVeterinario(vet);
        return mapper.toResponse(consultaRepository.save(consulta));
    }

    @Transactional
    @CacheEvict(value = {"consultas", "consulta"}, allEntries = true)
    public void deletar(Long id) {
        consultaRepository.delete(buscarEntidade(id));
    }

    private ConsultaEntity buscarEntidade(Long id) {
        return EntityFinder.findOrThrow(consultaRepository, id, "Consulta");
    }
}