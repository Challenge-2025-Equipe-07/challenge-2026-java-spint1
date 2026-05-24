package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.ExameMedicamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExameMedicamentoRepository extends JpaRepository<ExameMedicamentoEntity,Long> {
}
