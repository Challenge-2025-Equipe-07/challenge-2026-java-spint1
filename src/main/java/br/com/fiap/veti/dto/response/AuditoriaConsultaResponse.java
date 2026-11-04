package br.com.fiap.veti.dto.response;

import java.time.LocalDateTime;

public record AuditoriaConsultaResponse(Long id, String usuario, String operacao, LocalDateTime dataHora,
                                        String valoresAnteriores, String valoresNovos) {
}
