package br.com.fiap.veti.repository;

import br.com.fiap.veti.entity.VeterinarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface VeterinarioRepository extends JpaRepository<VeterinarioEntity, Long> {

    UserDetails findByEmail(String email);
}
