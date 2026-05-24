package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.ExameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExameRepository extends JpaRepository<ExameEntity, Long> {
}
