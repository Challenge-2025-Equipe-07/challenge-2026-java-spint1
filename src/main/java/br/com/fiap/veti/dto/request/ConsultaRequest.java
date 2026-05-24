package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConsultaRequest(@NotNull String dataConsulta, @NotNull long idVeterinario, @NotNull long idPet,
                              String descricao) {
}
