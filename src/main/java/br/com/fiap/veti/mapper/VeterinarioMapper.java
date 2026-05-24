package br.com.fiap.veti.mapper;

import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.dto.response.VeterinarioResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VeterinarioMapper {

    @Mapping(target = "email", source = "email")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", source = "role")
    VeterinarioEntity toEntity(RegisterRequest registerRequest);

    @Mapping(target = "email", source = "email")
    @Mapping(target = "id", source = "id")
    VeterinarioResponse toResponse (VeterinarioEntity entity);
}
