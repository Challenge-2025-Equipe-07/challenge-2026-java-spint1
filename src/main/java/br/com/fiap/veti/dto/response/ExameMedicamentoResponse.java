package br.com.fiap.veti.dto.response;

public record ExameMedicamentoResponse(long id,
                                       int quantidadeMedicamento,
                                       String posologia,
                                       long idExame,
                                       long idMedicamento) {
}
