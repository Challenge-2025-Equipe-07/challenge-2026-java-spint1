package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MedicamentoRequest(@NotBlank String nome,@NotBlank String descricao) {
}
