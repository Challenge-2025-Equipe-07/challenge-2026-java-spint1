package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.response.VeterinarioResponse;
import br.com.fiap.veti.service.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/veterinarios")
@RequiredArgsConstructor
@Tag(name = "Veterinários", description = "Gerenciamento de veterinários")
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    @GetMapping("/painel")
    @Operation(summary = "Painel HTML com lista de veterinários")
    public ModelAndView painel() {
        var veterinarios = veterinarioService.listarTodos(PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("email")));
        var mv = new ModelAndView("veterinarios-painel");
        mv.addObject("veterinarios", veterinarios.getContent());
        return mv;
    }

    @GetMapping
    @Operation(summary = "Listar todos os veterinários paginados")
    public ResponseEntity<Page<VeterinarioResponse>> listarTodos(
            @PageableDefault(size = 10, sort = "email") Pageable pageable) {
        return ResponseEntity.ok(veterinarioService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veterinário por ID")
    public ResponseEntity<VeterinarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(veterinarioService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar veterinário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        veterinarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}