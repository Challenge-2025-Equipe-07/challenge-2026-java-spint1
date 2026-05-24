package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.TutorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<TutorEntity, Long> {

    Optional<TutorEntity> findByCpf(String cpf);

    Optional<TutorEntity> findByEmail(String email);
}
