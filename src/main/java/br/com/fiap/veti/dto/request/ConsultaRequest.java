package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConsultaRequest(@NotNull LocalDateTime dataConsulta, @NotNull Long idVeterinario, @NotNull Long idPet,
                              String descricao) {
}
