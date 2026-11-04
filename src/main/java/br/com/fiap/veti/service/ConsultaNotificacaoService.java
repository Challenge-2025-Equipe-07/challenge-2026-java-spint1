package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.response.NotificacaoConsultaResponse;
import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ConsultaNotificacaoService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ConsultaRepository consultaRepository;
    private final EmailService emailService;

    public NotificacaoConsultaResponse notificarTutor(Long consultaId) {
        ConsultaEntity consulta = consultaRepository.findByIdComPetETutor(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));

        var tutor = consulta.getPet().getTutor();
        String pet = consulta.getPet().getNome();
        String data = consulta.getDataConsulta().format(DATA);
        String veterinario = consulta.getVeterinario().getEmail();

        String mensagem = String.format(
                "Olá %s, a consulta do pet %s está agendada para %s (veterinário: %s).",
                tutor.getNome(), pet, data, veterinario);

        String assunto = "Lembrete de consulta - " + pet;
        boolean enviado = emailService.enviar(tutor.getEmail(), assunto, mensagem,
                montarHtml(tutor.getNome(), pet, data, veterinario));

        return new NotificacaoConsultaResponse(
                tutor.getNome(),
                tutor.getEmail(),
                mensagem,
                LocalDateTime.now(),
                enviado ? "EMAIL" : "SIMULADO"
        );
    }

    private String montarHtml(String tutor, String pet, String data, String veterinario) {
        return """
                <div style="font-family:Inter,Arial,sans-serif;background:#f2edf8;padding:24px">
                  <div style="max-width:480px;margin:0 auto;background:#fcfbfe;border-radius:8px;padding:24px;color:#1e0930">
                    <h1 style="margin:0 0 16px;color:#8b3fbf;font-size:28px">Veti</h1>
                    <p style="font-size:16px">Olá, %s!</p>
                    <p style="font-size:16px">Este é um lembrete da consulta do <strong>%s</strong>:</p>
                    <p style="font-size:20px;color:#8b3fbf;margin:16px 0"><strong>%s</strong></p>
                    <p style="font-size:14px;color:#6b5b78">Veterinário: %s</p>
                    <p style="font-size:14px;color:#6b5b78">Se não puder comparecer, entre em contato com a clínica.</p>
                  </div>
                </div>
                """.formatted(HtmlUtils.htmlEscape(tutor), HtmlUtils.htmlEscape(pet),
                HtmlUtils.htmlEscape(data), HtmlUtils.htmlEscape(veterinario));
    }
}
