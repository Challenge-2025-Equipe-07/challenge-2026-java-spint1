package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.ConsultaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {
    Page<ConsultaEntity> findByPetId(Long petId, Pageable pageable);
    Page<ConsultaEntity> findByVeterinarioId(Long veterinarioId, Pageable pageable);
}