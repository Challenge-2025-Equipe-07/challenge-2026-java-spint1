package br.com.fiap.veti.dto.response;

public record ExameMedicamentoResponse(Long id,
                                       int quantidadeMedicamento,
                                       String posologia,
                                       Long idExame,
                                       Long idMedicamento) {
}
