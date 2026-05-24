package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.request.PetRequest;
import br.com.fiap.veti.dto.response.PetResponse;
import br.com.fiap.veti.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Gerenciamento de pets")
public class PetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "Listar todos os pets paginados")
    public ResponseEntity<Page<PetResponse>> listarTodos(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(petService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public ResponseEntity<PetResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(petService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo pet")
    public ResponseEntity<PetResponse> criar(
            @RequestBody @Valid PetRequest request,
            UriComponentsBuilder uriBuilder) {
        PetResponse response = petService.criar(request);
        var uri = uriBuilder.path("/pets/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet completo")
    public ResponseEntity<PetResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid PetRequest request) {
        return ResponseEntity.ok(petService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pet")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        petService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}