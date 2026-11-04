package br.com.fiap.veti.service;

import br.com.fiap.veti.repository.procedure.VetiPackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.UncategorizedSQLException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private VetiPackageRepository vetiPackageRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    private static UncategorizedSQLException erroOracle(int codigo, String mensagem) {
        return new UncategorizedSQLException("call", "PKG_VETI", new SQLException(mensagem, "72000", codigo));
    }

    @Test
    void consultasJson_deveDevolverOJsonDaProcedure() {
        // Arrange
        when(vetiPackageRepository.consultasJson()).thenReturn("[{\"consulta_id\":1}]");

        // Act
        String json = relatorioService.consultasJson();

        // Assert
        assertThat(json).isEqualTo("[{\"consulta_id\":1}]");
    }

    @Test
    void consultasJson_deveConverterRaiseApplicationErrorEmRegraDeNegocio() {
        // Arrange
        when(vetiPackageRepository.consultasJson()).thenThrow(erroOracle(20011,
                "ORA-20011: PRC_CONSULTAS_JSON: valor invalido ao converter a consulta para JSON.\nORA-06512: at \"VETI.PKG_VETI\", line 140"));

        // Act / Assert
        assertThatThrownBy(() -> relatorioService.consultasJson())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PRC_CONSULTAS_JSON: valor invalido ao converter a consulta para JSON.");
    }

    @Test
    void resumoConsultas_deveManterErrosDeInfraestruturaDoBanco() {
        // Arrange
        UncategorizedSQLException conexao = erroOracle(17002, "IO Error: The Network Adapter could not establish the connection");
        when(vetiPackageRepository.resumoConsultas()).thenThrow(conexao);

        // Act / Assert
        assertThatThrownBy(() -> relatorioService.resumoConsultas()).isSameAs(conexao);
    }

    @Test
    void mensagemOracle_deveRemoverCodigoEPilhaDoOracle() {
        // Arrange
        String mensagem = "ORA-20002: FN_MONTA_JSON: quantidade de chaves e valores diferente.\nORA-06512: at line 1";

        // Act
        String resultado = RelatorioService.mensagemOracle(mensagem);

        // Assert
        assertThat(resultado).isEqualTo("FN_MONTA_JSON: quantidade de chaves e valores diferente.");
    }
}
