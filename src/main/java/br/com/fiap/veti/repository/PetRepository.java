package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<PetEntity, Long> {
}
