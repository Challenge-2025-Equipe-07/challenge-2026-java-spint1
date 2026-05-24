package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExameRequest(@NotNull String descricao, @NotNull long idConsulta, @NotNull List<ExameMedicamentoRequest> receitaPedidoMedicamento) {
}
