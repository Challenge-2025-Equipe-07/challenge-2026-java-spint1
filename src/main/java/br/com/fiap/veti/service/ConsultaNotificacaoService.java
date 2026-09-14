package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.response.NotificacaoConsultaResponse;
import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsultaNotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaNotificacaoService.class);

    private final ConsultaRepository consultaRepository;

    public NotificacaoConsultaResponse notificarTutor(Long consultaId) {
        ConsultaEntity consulta = consultaRepository.findByIdComPetETutor(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));

        var tutor = consulta.getPet().getTutor();

        String mensagem = String.format(
                "Olá %s, sua consulta para o pet %s está agendada para %s (veterinário: %s).",
                tutor.getNome(),
                consulta.getPet().getNome(),
                consulta.getDataConsulta(),
                consulta.getVeterinario().getEmail()
        );

        log.info("[NOTIFICAÇÃO SIMULADA] Para: {} | {}", tutor.getEmail(), mensagem);

        return new NotificacaoConsultaResponse(
                tutor.getNome(),
                tutor.getEmail(),
                mensagem,
                LocalDateTime.now()
        );
    }
}