package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.response.AuditoriaConsultaResponse;
import br.com.fiap.veti.repository.procedure.VetiPackageRepository;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

/**
 * Relatórios gerados pelo package PKG_VETI (procedures) e pela trigger de auditoria do Oracle.
 */
@Slf4j
@Service
@Observed(name = "veti.service")
@RequiredArgsConstructor
public class RelatorioService {

    private static final int LIMITE_AUDITORIA = 50;

    private final VetiPackageRepository vetiPackageRepository;

    public String consultasJson() {
        return chamar("PRC_CONSULTAS_JSON", vetiPackageRepository::consultasJson);
    }

    public String resumoConsultas() {
        return chamar("PRC_RESUMO_CONSULTAS", vetiPackageRepository::resumoConsultas);
    }

    public String resumoReceitas() {
        return chamar("PRC_RESUMO_RECEITAS", vetiPackageRepository::resumoReceitas);
    }

    public String exportarDataset() {
        return chamar("PRC_EXPORTAR_DATASET", vetiPackageRepository::exportarDataset);
    }

    public List<AuditoriaConsultaResponse> auditoriaConsultas() {
        return vetiPackageRepository.ultimasAuditorias(LIMITE_AUDITORIA);
    }

    /**
     * Erros de negócio do PL-SQL (RAISE_APPLICATION_ERROR, ORA-20000 a ORA-20999) viram IllegalArgumentException,
     * tratada pelo ControllerAdvice (409) e pelas telas; os demais erros de banco seguem como estão.
     */
    static <T> T chamar(String objeto, Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (DataAccessException e) {
            SQLException sql = sqlException(e);
            if (sql != null && sql.getErrorCode() >= 20000 && sql.getErrorCode() <= 20999) {
                log.warn("Regra de negocio do Oracle em {}: {}", objeto, sql.getMessage());
                throw new IllegalArgumentException(mensagemOracle(sql.getMessage()), e);
            }
            throw e;
        }
    }

    private static SQLException sqlException(Throwable e) {
        for (Throwable atual = e; atual != null; atual = atual.getCause()) {
            if (atual instanceof SQLException sql) {
                return sql;
            }
        }
        return null;
    }

    // "ORA-20002: FN_MONTA_JSON: ...\nORA-06512: at ..." -> "FN_MONTA_JSON: ..."
    static String mensagemOracle(String mensagem) {
        if (mensagem == null) {
            return "Erro no banco de dados";
        }
        String primeiraLinha = mensagem.lines().findFirst().orElse(mensagem);
        return primeiraLinha.replaceFirst("^ORA-\\d{5}:\\s*", "");
    }
}
