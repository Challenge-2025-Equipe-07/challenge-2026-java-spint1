package br.com.fiap.veti.dto.response;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        LocalDateTime dataConsulta,
        Long petId,
        String petNome,
        Long tutorId,
        String tutorNome,
        Long veterinarioId,
        String veterinarioEmail
) {}
