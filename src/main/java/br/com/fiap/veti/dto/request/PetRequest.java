package br.com.fiap.veti.dto.request;

import br.com.fiap.veti.enms.Raca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PetRequest(@NotBlank String name, @NotNull Raca raca, @NotNull Double peso, @NotNull int idade,
                         @NotNull boolean castrado,
                         @NotNull Long idTutor) {
}
