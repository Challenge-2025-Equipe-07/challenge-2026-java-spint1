package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record ConsultaRequest(@NotNull(message = "Informe a data e hora") @Future(message = "A consulta deve ser agendada para uma data futura")
                              @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataConsulta,
                              @NotNull(message = "Selecione o veterinário") Long idVeterinario,
                              @NotNull(message = "Selecione o pet") Long idPet,
                              String descricao) {
}
