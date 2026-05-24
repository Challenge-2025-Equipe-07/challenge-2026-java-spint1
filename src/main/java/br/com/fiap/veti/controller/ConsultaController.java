package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.request.ConsultaRequest;
import br.com.fiap.veti.dto.response.ConsultaResponse;
import br.com.fiap.veti.service.ConsultaService;
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
@RequestMapping("/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Gerenciamento de consultas veterinárias")
public class ConsultaController {

    private final ConsultaService consultaService;

    @GetMapping
    @Operation(summary = "Listar todas as consultas paginadas")
    public ResponseEntity<Page<ConsultaResponse>> listarTodos(
            @PageableDefault(size = 10, sort = "dataConsulta") Pageable pageable) {
        return ResponseEntity.ok(consultaService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar consulta por ID")
    public ResponseEntity<ConsultaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova consulta")
    public ResponseEntity<ConsultaResponse> criar(
            @RequestBody @Valid ConsultaRequest request,
            UriComponentsBuilder uriBuilder) {
        ConsultaResponse response = consultaService.criar(request);
        var uri = uriBuilder.path("/consultas/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar consulta completa")
    public ResponseEntity<ConsultaResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ConsultaRequest request) {
        return ResponseEntity.ok(consultaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar consulta")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        consultaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}