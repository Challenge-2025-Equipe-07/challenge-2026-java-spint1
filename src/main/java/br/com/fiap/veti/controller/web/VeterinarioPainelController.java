package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.enms.Role;
import br.com.fiap.veti.service.VeterinarioService;
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

import java.util.Map;

/**
 * Gestão da equipe (somente ADMIN, ver SecurityConfigurations): aqui o ADMIN cria contas USER ou ADMIN.
 */
@Controller
@RequestMapping("/veterinarios/painel")
@RequiredArgsConstructor
public class VeterinarioPainelController {

    private final VeterinarioService veterinarioService;
    private final MessageSource messageSource;

    @GetMapping
    public String painel(Model model) {
        return carregar(model, null, Map.of());
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") RegisterRequest form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return carregar(model, form, FormErrors.of(result, messageSource));
        }
        try {
            veterinarioService.cadastrar(form, form.role() != null ? form.role() : Role.USER);
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            return carregar(model, form, Map.of());
        }
        redirect.addFlashAttribute("sucesso", "Conta " + form.email() + " criada.");
        return "redirect:/veterinarios/painel";
    }

    private String carregar(Model model, RegisterRequest form, Map<String, String> erros) {
        model.addAttribute("veterinarios", veterinarioService.listarTodos(PageRequest.of(0, 500, Sort.by("email"))).getContent());
        model.addAttribute("roles", Role.values());
        model.addAttribute("form", form);
        model.addAttribute("erros", erros);
        model.addAttribute("pagina", "veterinarios");
        return "veterinarios-painel";
    }
}
