package br.com.fiap.veti.controller;

import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.repository.ConsultaRepository;
import br.com.fiap.veti.service.ConsultaNotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/consultas")
public class ConsultaPainelController {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ConsultaNotificacaoService consultaNotificacaoService;

    @GetMapping("/painel")
    public String painel(Model model) {
        List<ConsultaEntity> consultas = consultaRepository.findAllComPetETutor();
        model.addAttribute("consultas", consultas);
        return "consultas-painel";
    }

    @PostMapping("/painel/{id}/notificar")
    public String notificar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var resultado = consultaNotificacaoService.notificarTutor(id);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Notificação simulada enviada para " + resultado.destinatarioNome()
                            + " (" + resultado.destinatarioEmail() + ")");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", "Consulta não encontrada.");
        }
        return "redirect:/consultas/painel";
    }
}