package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.response.NotificacaoConsultaResponse;
import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultaNotificacaoService {

    @Autowired
    private ConsultaRepository consultaRepository;

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

        System.out.println("[NOTIFICAÇÃO SIMULADA] Para: " + tutor.getEmail() + " | " + mensagem);

        return new NotificacaoConsultaResponse(
                tutor.getNome(),
                tutor.getEmail(),
                mensagem,
                LocalDateTime.now()
        );
    }
}