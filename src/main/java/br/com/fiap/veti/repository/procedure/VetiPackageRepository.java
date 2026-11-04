package br.com.fiap.veti.repository.procedure;

import br.com.fiap.veti.dto.response.AuditoriaConsultaResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Acesso ao package Oracle PKG_VETI e à TB_AUDITORIA (src/main/resources/db/pkg_veti.sql).
 * Os parâmetros são declarados explicitamente para não depender da leitura de metadados do driver.
 */
@Repository
public class VetiPackageRepository {

    private static final String PACKAGE = "PKG_VETI";

    // Lê o CLOB enquanto a conexão ainda está aberta
    private static final SqlReturnType CLOB_PARA_TEXTO = (cs, index, sqlType, typeName) -> {
        Clob clob = cs.getClob(index);
        return clob == null ? null : clob.getSubString(1, (int) clob.length());
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcCall validaSenha;
    private final SimpleJdbcCall consultasJson;
    private final SimpleJdbcCall resumoConsultas;

    public VetiPackageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.validaSenha = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE)
                .withFunctionName("FN_VALIDA_SENHA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlOutParameter("RETURN", Types.VARCHAR),
                        new SqlParameter("P_SENHA", Types.VARCHAR));
        this.consultasJson = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE)
                .withProcedureName("PRC_CONSULTAS_JSON")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlOutParameter("P_JSON", Types.CLOB, null, CLOB_PARA_TEXTO));
        this.resumoConsultas = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName(PACKAGE)
                .withProcedureName("PRC_RESUMO_CONSULTAS")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(new SqlOutParameter("P_RESUMO", Types.CLOB, null, CLOB_PARA_TEXTO));
    }

    /** PKG_VETI.FN_VALIDA_SENHA: devolve 'SENHA_VALIDA' ou 'ERRO: motivo'. */
    public String validarSenha(String senha) {
        return validaSenha.executeFunction(String.class, Map.of("P_SENHA", senha));
    }

    /** PKG_VETI.PRC_CONSULTAS_JSON: array JSON montado manualmente pela FN_MONTA_JSON. */
    public String consultasJson() {
        return (String) saida(consultasJson.execute(), "P_JSON");
    }

    /** PKG_VETI.PRC_RESUMO_CONSULTAS: relatório com subtotal por veterinário e total geral. */
    public String resumoConsultas() {
        return (String) saida(resumoConsultas.execute(), "P_RESUMO");
    }

    /** Últimos registros gravados pela trigger TRG_AUDITORIA_CONSULTA. */
    public List<AuditoriaConsultaResponse> ultimasAuditorias(int limite) {
        return jdbcTemplate.query("""
                        SELECT ID_AUDITORIA, NM_USUARIO, TP_OPERACAO, DT_OPERACAO,
                               DS_VALORES_ANTERIORES, DS_VALORES_NOVOS
                        FROM TB_AUDITORIA
                        ORDER BY ID_AUDITORIA DESC
                        FETCH FIRST ? ROWS ONLY""",
                (rs, rowNum) -> {
                    Timestamp dataHora = rs.getTimestamp("DT_OPERACAO");
                    return new AuditoriaConsultaResponse(
                            rs.getLong("ID_AUDITORIA"),
                            rs.getString("NM_USUARIO"),
                            rs.getString("TP_OPERACAO"),
                            dataHora != null ? dataHora.toLocalDateTime() : null,
                            rs.getString("DS_VALORES_ANTERIORES"),
                            rs.getString("DS_VALORES_NOVOS"));
                },
                limite);
    }

    // O driver pode devolver o nome do parâmetro em maiúsculas ou minúsculas
    private static Object saida(Map<String, Object> resultado, String parametro) {
        return resultado.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(parametro))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
