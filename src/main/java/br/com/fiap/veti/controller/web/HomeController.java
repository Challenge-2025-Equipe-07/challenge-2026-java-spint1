package br.com.fiap.veti.controller.web;

import br.com.fiap.veti.repository.ConsultaRepository;
import br.com.fiap.veti.repository.PetRepository;
import br.com.fiap.veti.repository.TutorRepository;
import br.com.fiap.veti.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final int PROXIMAS_NO_PAINEL = 5;

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;

    @GetMapping({"/", "/dashboard"})
    public String home(Model model) {
        var proximas = consultaRepository.findProximasComPetETutor(LocalDateTime.now());
        long hoje = proximas.stream()
                .filter(c -> c.getDataConsulta().toLocalDate().equals(LocalDate.now()))
                .count();

        model.addAttribute("totalTutores", tutorRepository.count());
        model.addAttribute("totalPets", petRepository.count());
        model.addAttribute("totalVeterinarios", veterinarioRepository.count());
        model.addAttribute("consultasFuturas", proximas.size());
        model.addAttribute("consultasHoje", hoje);
        model.addAttribute("proximas", proximas.stream().limit(PROXIMAS_NO_PAINEL).toList());
        model.addAttribute("pagina", "inicio");
        return "home";
    }
}
