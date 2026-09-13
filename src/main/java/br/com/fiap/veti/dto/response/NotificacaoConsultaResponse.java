package br.com.fiap.veti.dto.response;

import java.time.LocalDateTime;

public record NotificacaoConsultaResponse(
        String destinatarioNome,
        String destinatarioEmail,
        String mensagem,
        LocalDateTime enviadoEm
) {}