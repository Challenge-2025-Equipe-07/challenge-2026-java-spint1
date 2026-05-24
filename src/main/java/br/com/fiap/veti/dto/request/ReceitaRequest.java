package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReceitaRequest(@NotBlank String descricao, @NotNull long idConsulta,@NotNull List<ReceitaMedicamentoRequest> receitaPedidoMedicamento) {
}
