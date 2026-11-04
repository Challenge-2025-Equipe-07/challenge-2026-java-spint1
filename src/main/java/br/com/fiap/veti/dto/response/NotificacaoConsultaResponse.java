package br.com.fiap.veti.dto.response;

import java.time.LocalDateTime;

/**
 * @param canal EMAIL quando enviado pelo SMTP; SIMULADO quando o SMTP não está configurado e a mensagem só foi registrada no log
 */
public record NotificacaoConsultaResponse(
        String destinatarioNome,
        String destinatarioEmail,
        String mensagem,
        LocalDateTime enviadoEm,
        String canal
) {}
