package br.com.fiap.veti.dto.response;

public record ReceitaMedicamentoResponse(long id,
                                         int quantidadeMedicamento,
                                         String posologia,
                                         long idReceita,
                                         long idMedicamento) {
}
