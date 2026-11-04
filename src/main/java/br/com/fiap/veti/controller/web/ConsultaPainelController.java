package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.controller.web.WebModelAdvice.UsuarioLogado;
import br.com.fiap.veti.dto.request.ConsultaRequest;
import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.repository.ConsultaRepository;
import br.com.fiap.veti.exception.EmailNaoEnviadoException;
import br.com.fiap.veti.service.ConsultaNotificacaoService;
import br.com.fiap.veti.service.ConsultaService;
import br.com.fiap.veti.service.PetService;
import br.com.fiap.veti.service.VeterinarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/consultas/painel")
@RequiredArgsConstructor
public class ConsultaPainelController {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ConsultaRepository consultaRepository;
    private final ConsultaService consultaService;
    private final ConsultaNotificacaoService consultaNotificacaoService;
    private final PetService petService;
    private final VeterinarioService veterinarioService;
    private final MessageSource messageSource;

    @GetMapping
    public String painel(Model model, @ModelAttribute("usuario") UsuarioLogado usuario) {
        // Pré-seleciona o veterinário logado no formulário de agendamento
        var form = new ConsultaRequest(null, usuario != null ? usuario.id() : null, null, null);
        return carregar(model, form, Map.of());
    }

    @PostMapping
    public String agendar(@Valid @ModelAttribute("form") ConsultaRequest form, BindingResult result,
                          Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return carregar(model, form, FormErrors.of(result, messageSource));
        }
        try {
            var consulta = consultaService.criar(form);
            redirect.addFlashAttribute("sucesso", "Consulta de " + consulta.petNome() + " agendada para "
                    + consulta.dataConsulta().format(DATA_HORA) + ".");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            model.addAttribute("erro", e.getMessage());
            return carregar(model, form, Map.of());
        }
        return "redirect:/consultas/painel";
    }

    @PostMapping("/{id}/notificar")
    public String notificar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            var resultado = consultaNotificacaoService.notificarTutor(id);
            String destino = resultado.destinatarioNome() + " (" + resultado.destinatarioEmail() + ")";
            redirect.addFlashAttribute("sucesso", "EMAIL".equals(resultado.canal())
                    ? "Lembrete enviado por e-mail para " + destino + "."
                    : "Lembrete registrado para " + destino + " (envio de e-mail não configurado).");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("erro", "Consulta não encontrada.");
        } catch (EmailNaoEnviadoException e) {
            redirect.addFlashAttribute("erro", e.getMessage() + " Tente novamente em instantes.");
        }
        return "redirect:/consultas/painel";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            consultaService.deletar(id);
            redirect.addFlashAttribute("sucesso", "Consulta cancelada.");
        } catch (EntityNotFoundException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/consultas/painel";
    }

    private String carregar(Model model, ConsultaRequest form, Map<String, String> erros) {
        LocalDateTime agora = LocalDateTime.now();
        List<ConsultaEntity> consultas = consultaRepository.findAllComPetETutor();
        model.addAttribute("proximas", consultas.stream()
                .filter(c -> !c.getDataConsulta().isBefore(agora))
                .toList());
        model.addAttribute("anteriores", consultas.stream()
                .filter(c -> c.getDataConsulta().isBefore(agora))
                .sorted(Comparator.comparing(ConsultaEntity::getDataConsulta).reversed())
                .toList());
        model.addAttribute("pets", petService.listarTodos(PageRequest.of(0, 500, Sort.by("nome"))).getContent());
        model.addAttribute("veterinarios", veterinarioService.listarTodos(PageRequest.of(0, 500, Sort.by("email"))).getContent());
        model.addAttribute("form", form);
        model.addAttribute("erros", erros);
        model.addAttribute("pagina", "consultas");
        return "consultas-painel";
    }
}
