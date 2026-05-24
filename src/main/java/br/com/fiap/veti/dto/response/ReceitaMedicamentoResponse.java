package br.com.fiap.veti.dto.response;

public record ReceitaMedicamentoResponse(Long id,
                                         int quantidadeMedicamento,
                                         String posologia,
                                         Long idReceita,
                                         Long idMedicamento) {
}
