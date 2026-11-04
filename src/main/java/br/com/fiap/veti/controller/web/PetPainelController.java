package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.dto.request.PetRequest;
import br.com.fiap.veti.dto.response.TutorResponse;
import br.com.fiap.veti.enms.Raca;
import br.com.fiap.veti.service.PetService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pets/painel")
@RequiredArgsConstructor
public class PetPainelController {

    private final PetService petService;
    private final TutorService tutorService;
    private final MessageSource messageSource;

    @GetMapping
    public String painel(Model model) {
        return carregar(model, null, Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") PetRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return carregar(model, form, FormErrors.of(result, messageSource));
        }
        try {
            petService.criar(form);
        } catch (EntityNotFoundException e) {
            model.addAttribute("erro", e.getMessage());
            return carregar(model, form, Map.of());
        }
        redirect.addFlashAttribute("sucesso", "Pet " + form.name() + " cadastrado.");
        return "redirect:/pets/painel";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            petService.deletar(id);
            redirect.addFlashAttribute("sucesso", "Pet excluído.");
        } catch (EntityNotFoundException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Não é possível excluir: o pet possui consultas registradas.");
        }
        return "redirect:/pets/painel";
    }

    private String carregar(Model model, PetRequest form, Map<String, String> erros) {
        var tutores = tutorService.listarTodos(PageRequest.of(0, 500, Sort.by("nome"))).getContent();
        model.addAttribute("pets", petService.listarTodos(PageRequest.of(0, 500, Sort.by("nome"))).getContent());
        model.addAttribute("tutores", tutores);
        model.addAttribute("nomeTutor", tutores.stream()
                .collect(Collectors.toMap(TutorResponse::id, TutorResponse::name, (a, b) -> a)));
        model.addAttribute("racas", Raca.values());
        model.addAttribute("form", form);
        model.addAttribute("erros", erros);
        model.addAttribute("pagina", "pets");
        return "pets-painel";
    }
}
