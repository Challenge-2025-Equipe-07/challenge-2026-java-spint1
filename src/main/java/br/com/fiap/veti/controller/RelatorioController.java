package br.com.fiap.veti.controller;

import br.com.fiap.veti.dto.response.AuditoriaConsultaResponse;
import br.com.fiap.veti.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Relatórios do package Oracle PKG_VETI (somente ADMIN)")
public class RelatorioController {

    private static final String ARQUIVO_DATASET = "veti_dataset.json";

    private final RelatorioService relatorioService;

    @GetMapping(value = "/consultas-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consultas em JSON montado pela procedure PKG_VETI.PRC_CONSULTAS_JSON")
    public ResponseEntity<String> consultasJson() {
        return ResponseEntity.ok(relatorioService.consultasJson());
    }

    @GetMapping(value = "/resumo-consultas", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Consultas por veterinário e pet, com subtotal e total (PKG_VETI.PRC_RESUMO_CONSULTAS)")
    public ResponseEntity<String> resumoConsultas() {
        return ResponseEntity.ok(relatorioService.resumoConsultas());
    }

    @GetMapping(value = "/resumo-receitas", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Quantidade de medicamentos por receita, com subtotal e total (PKG_VETI.PRC_RESUMO_RECEITAS)")
    public ResponseEntity<String> resumoReceitas() {
        return ResponseEntity.ok(relatorioService.resumoReceitas());
    }

    @GetMapping(value = "/dataset-json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Banco relacional exportado em JSON para o MongoDB (PKG_VETI.PRC_EXPORTAR_DATASET)")
    public ResponseEntity<String> exportarDataset(@RequestParam(defaultValue = "false") boolean download) {
        ResponseEntity.BodyBuilder resposta = ResponseEntity.ok();
        if (download) {
            resposta.header(HttpHeaders.CONTENT_DISPOSITION,
                    ContentDisposition.attachment().filename(ARQUIVO_DATASET).build().toString());
        }
        return resposta.body(relatorioService.exportarDataset());
    }

    @GetMapping("/auditoria-consultas")
    @Operation(summary = "Últimas alterações em consultas registradas pela trigger TRG_AUDITORIA_CONSULTA")
    public ResponseEntity<List<AuditoriaConsultaResponse>> auditoriaConsultas() {
        return ResponseEntity.ok(relatorioService.auditoriaConsultas());
    }
}
