package br.com.fiap.veti.dto.request;

import br.com.fiap.veti.enms.Raca;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PetRequest(@NotBlank(message = "Informe o nome") @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres") String name,
                         @NotNull(message = "Selecione a raça") Raca raca,
                         @NotNull(message = "Informe o peso") @Positive(message = "Peso deve ser maior que zero") Double peso,
                         @NotNull(message = "Informe a idade") @Min(value = 0, message = "Idade não pode ser negativa") @Max(value = 99, message = "Idade deve ser no máximo 99") Integer idade,
                         Boolean castrado,
                         @NotNull(message = "Selecione o tutor") Long idTutor) {
}
