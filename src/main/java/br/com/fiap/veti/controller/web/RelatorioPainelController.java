package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Tela dos relatórios do banco (somente ADMIN, ver SecurityConfigurations): mostra o resultado das procedures
 * do PKG_VETI e o que a trigger de auditoria gravou.
 */
@Controller
@RequestMapping("/relatorios/painel")
@RequiredArgsConstructor
public class RelatorioPainelController {

    private final RelatorioService relatorioService;

    @GetMapping
    public String painel(Model model) {
        try {
            // uma consulta por linha para facilitar a leitura na tela
            model.addAttribute("consultasJson", relatorioService.consultasJson().replace("},{", "},\n{"));
            model.addAttribute("resumo", relatorioService.resumoConsultas());
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
        }
        model.addAttribute("auditoria", relatorioService.auditoriaConsultas());
        model.addAttribute("pagina", "relatorios");
        return "relatorios-painel";
    }
}
