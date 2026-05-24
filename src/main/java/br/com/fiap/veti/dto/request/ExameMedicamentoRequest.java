package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExameMedicamentoRequest(@NotNull int quantidadeMedicamento,
                                      @NotBlank String posologia,
                                      @NotNull long idExame,
                                      @NotNull long idMedicamento) {
}
