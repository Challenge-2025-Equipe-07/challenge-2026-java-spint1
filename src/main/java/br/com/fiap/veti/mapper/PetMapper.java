package br.com.fiap.veti.mapper;

import br.com.fiap.veti.dto.response.PetResponse;
import br.com.fiap.veti.entity.PetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(target = "id")
    @Mapping(target = "nome")
    @Mapping(target = "raca")
    @Mapping(target = "peso")
    @Mapping(target = "idade")
    @Mapping(target = "castrado", source = ".", qualifiedByName = "mapCastrado")
    @Mapping(target = "idTutor", source = "entity.tutor.id")
    PetResponse toResponse(PetEntity entity);

    @Named("mapCastrado")
    default boolean mapCastrado(PetEntity entity){
        return true;
    }
}
