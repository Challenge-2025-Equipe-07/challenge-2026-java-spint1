package br.com.fiap.veti.mapper;

import br.com.fiap.veti.dto.response.ConsultaResponse;
import br.com.fiap.veti.entity.ConsultaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultaMapper {



    @Mapping(target = "petId",           source = "pet.id")
    @Mapping(target = "petNome",         source = "pet.nome")
    @Mapping(target = "tutorId",         source = "pet.tutor.id")
    @Mapping(target = "tutorNome",       source = "pet.tutor.nome")
    @Mapping(target = "veterinarioId",   source = "veterinario.id")
    @Mapping(target = "veterinarioEmail",source = "veterinario.email")
    ConsultaResponse toResponse(ConsultaEntity consulta);
}