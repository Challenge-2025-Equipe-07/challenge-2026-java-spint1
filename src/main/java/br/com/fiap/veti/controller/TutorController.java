package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.request.TutorRequest;
import br.com.fiap.veti.dto.response.TutorResponse;
import br.com.fiap.veti.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/tutores")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Gerenciamento de tutores")
public class TutorController {

    private final TutorService tutorService;


    @GetMapping("/painel")
    @Operation(summary = "Painel HTML com lista de tutores")
    public ModelAndView painel() {
        var tutores = tutorService.listarTodos(PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("nome")));
        var mv = new ModelAndView("tutores-painel");
        mv.addObject("tutores", tutores.getContent());
        return mv;
    }

    @GetMapping
    @Operation(summary = "Listar todos os tutores paginados")
    public ResponseEntity<Page<TutorResponse>> listarTodos(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(tutorService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    public ResponseEntity<TutorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo tutor")
    public ResponseEntity<TutorResponse> criar(
            @RequestBody @Valid TutorRequest request,
            UriComponentsBuilder uriBuilder) {
        TutorResponse response = tutorService.criar(request);
        var uri = uriBuilder.path("/tutores/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tutor completo")
    public ResponseEntity<TutorResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid TutorRequest request) {
        return ResponseEntity.ok(tutorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tutor")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tutorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}