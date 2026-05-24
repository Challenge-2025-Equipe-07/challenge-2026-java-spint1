package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PetRequest(@NotBlank String name, @NotNull Enum raca, @NotNull Double peso, @NotNull int idade,
                         @NotNull boolean castrado,
                         @NotNull long idTutor) {
}
