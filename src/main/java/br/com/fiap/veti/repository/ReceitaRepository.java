package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.ReceitaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceitaRepository extends JpaRepository<ReceitaEntity,Long> {
}
