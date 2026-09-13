package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.ConsultaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {
    Page<ConsultaEntity> findByPetId(Long petId, Pageable pageable);
    Page<ConsultaEntity> findByVeterinarioId(Long veterinarioId, Pageable pageable);

    @Query("SELECT c FROM ConsultaEntity c " +
            "JOIN FETCH c.pet p " +
            "JOIN FETCH p.tutor " +
            "JOIN FETCH c.veterinario " +
            "WHERE c.id = :id")
    Optional<ConsultaEntity> findByIdComPetETutor(Long id);

    @Query("SELECT c FROM ConsultaEntity c " +
            "JOIN FETCH c.pet p " +
            "JOIN FETCH p.tutor " +
            "JOIN FETCH c.veterinario " +
            "ORDER BY c.dataConsulta")
    List<ConsultaEntity> findAllComPetETutor();
}