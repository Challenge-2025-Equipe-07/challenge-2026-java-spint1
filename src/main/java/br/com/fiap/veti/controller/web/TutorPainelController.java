package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.dto.request.TutorRequest;
import br.com.fiap.veti.service.TutorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/tutores/painel")
@RequiredArgsConstructor
public class TutorPainelController {

    private final TutorService tutorService;
    private final MessageSource messageSource;

    @GetMapping
    public String painel(Model model) {
        return carregar(model, null, Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") TutorRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return carregar(model, form, FormErrors.of(result, messageSource));
        }
        try {
            tutorService.criar(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            return carregar(model, form, Map.of());
        }
        redirect.addFlashAttribute("sucesso", "Tutor " + form.name() + " cadastrado.");
        return "redirect:/tutores/painel";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            tutorService.deletar(id);
            redirect.addFlashAttribute("sucesso", "Tutor excluído.");
        } catch (EntityNotFoundException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o tutor possui pets cadastrados.");
        }
        return "redirect:/tutores/painel";
    }

    private String carregar(Model model, TutorRequest form, Map<String, String> erros) {
        model.addAttribute("tutores", tutorService.listarTodos(PageRequest.of(0, 500, Sort.by("nome"))).getContent());
        model.addAttribute("form", form);
        model.addAttribute("erros", erros);
        model.addAttribute("pagina", "tutores");
        return "tutores-painel";
    }
}
