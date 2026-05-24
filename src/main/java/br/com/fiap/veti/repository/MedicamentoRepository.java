package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.MedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<MedicamentoEntity,Long> {
}
