package br.com.fiap.veti.dto.response;

import java.util.List;

public record AgendaPetResponse(List<ConsultaResponse> consultas) {
}
