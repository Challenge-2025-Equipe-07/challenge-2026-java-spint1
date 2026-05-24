package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.ReceitaMedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceitaMedicamentoRepository extends JpaRepository<ReceitaMedicamentoEntity, Long> {
}
