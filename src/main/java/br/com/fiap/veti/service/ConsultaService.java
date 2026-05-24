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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.util.Arrays.stream;
import static org.springframework.data.domain.Pageable.unpaged;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    @Autowired
    ConsultaRepository consultaRepository;
    @Autowired
    PetRepository petRepository;
    @Autowired
    VeterinarioRepository veterinarioRepository;
    @Autowired
    ConsultaMapper mapper;

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
        PetEntity pet = petRepository.findById(request.idPet())
                .orElseThrow(() -> new EntityNotFoundException("Pet não encontrado: " + request.idPet()));
        VeterinarioEntity vet = veterinarioRepository.findById(request.idVeterinario())
                .orElseThrow(() -> new EntityNotFoundException("Veterinário não encontrado: " + request.idVeterinario()));

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
        PetEntity pet = petRepository.findById(request.idPet())
                .orElseThrow(() -> new EntityNotFoundException("Pet não encontrado: " + request.idPet()));
        VeterinarioEntity vet = veterinarioRepository.findById(request.idVeterinario())
                .orElseThrow(() -> new EntityNotFoundException("Veterinário não encontrado: " + request.idVeterinario()));
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
        return consultaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consulta não encontrada: " + id));
    }
}