package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.response.NotificacaoConsultaResponse;
import br.com.fiap.veti.service.ConsultaNotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultas")
public class ConsultaNotificacaoController {

    @Autowired
    private ConsultaNotificacaoService consultaNotificacaoService;

    @PostMapping("/{id}/notificar-tutor")
    public ResponseEntity<NotificacaoConsultaResponse> notificarTutor(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(consultaNotificacaoService.notificarTutor(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}