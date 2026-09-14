package br.com.fiap.veti.mapper;

import br.com.fiap.veti.dto.response.PetResponse;
import br.com.fiap.veti.entity.PetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(target = "idTutor", source = "tutor.id")
    PetResponse toResponse(PetEntity entity);
}
