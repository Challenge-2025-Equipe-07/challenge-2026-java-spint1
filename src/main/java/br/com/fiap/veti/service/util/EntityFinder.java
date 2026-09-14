package br.com.fiap.veti.service.util;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;


public final class EntityFinder {

    private EntityFinder() {
    }

    public static <ENTITY, ID> ENTITY findOrThrow(JpaRepository<ENTITY, ID> repository, ID id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entityName + " não encontrado: " + id));
    }
}
