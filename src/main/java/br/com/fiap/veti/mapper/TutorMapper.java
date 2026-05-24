package br.com.fiap.veti.mapper;

import br.com.fiap.veti.dto.response.TutorResponse;
import br.com.fiap.veti.entity.TutorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TutorMapper {

    @Mapping(target = "id")
    @Mapping(target = "name", source = "nome")
    @Mapping(target = "email")
    @Mapping(target = "phone",source = "telefone")
    @Mapping(target = "cpf")
    TutorResponse toResponse(TutorEntity entity);
}
