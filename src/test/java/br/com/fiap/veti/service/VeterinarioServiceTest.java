package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.RegisterRequest;
import br.com.fiap.veti.dto.response.VeterinarioResponse;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.enms.Role;
import br.com.fiap.veti.mapper.VeterinarioMapper;
import br.com.fiap.veti.repository.VeterinarioRepository;
import br.com.fiap.veti.repository.procedure.VetiPackageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeterinarioServiceTest {

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private VeterinarioMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VetiPackageRepository vetiPackageRepository;

    @InjectMocks
    private VeterinarioService veterinarioService;

    private final RegisterRequest request = new RegisterRequest("vet@veti.com", "Senha1234", Role.ADMIN);

    @Test
    void cadastrar_deveSalvarComSenhaCriptografadaQuandoOracleAprovaASenha() {
        // Arrange
        when(veterinarioRepository.findByEmail("vet@veti.com")).thenReturn(null);
        when(vetiPackageRepository.validarSenha("Senha1234")).thenReturn("SENHA_VALIDA");
        when(mapper.toEntity(request)).thenReturn(VeterinarioEntity.builder().email("vet@veti.com").build());
        when(passwordEncoder.encode("Senha1234")).thenReturn("$2a$hash");
        when(veterinarioRepository.save(any(VeterinarioEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0, VeterinarioEntity.class));
        when(mapper.toResponse(any(VeterinarioEntity.class))).thenReturn(new VeterinarioResponse(1L, "vet@veti.com", "USER"));

        // Act
        veterinarioService.cadastrar(request, Role.USER);

        // Assert
        ArgumentCaptor<VeterinarioEntity> captor = ArgumentCaptor.forClass(VeterinarioEntity.class);
        verify(veterinarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$hash");
        assertThat(captor.getValue().getRole()).isEqualTo("USER");
    }

    @Test
    void cadastrar_deveRecusarSenhaReprovadaPelaFuncaoDoOracle() {
        // Arrange
        when(veterinarioRepository.findByEmail("vet@veti.com")).thenReturn(null);
        when(vetiPackageRepository.validarSenha("Senha1234"))
                .thenReturn("ERRO: a senha deve conter ao menos um numero.");

        // Act / Assert
        assertThatThrownBy(() -> veterinarioService.cadastrar(request, Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("a senha deve conter ao menos um numero.");
        verify(veterinarioRepository, never()).save(any());
    }

    @Test
    void cadastrar_deveRecusarEmailJaCadastradoSemChamarOBanco() {
        // Arrange
        when(veterinarioRepository.findByEmail("vet@veti.com")).thenReturn(new VeterinarioEntity());

        // Act / Assert
        assertThatThrownBy(() -> veterinarioService.cadastrar(request, Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já cadastrado");
        verify(vetiPackageRepository, never()).validarSenha(any());
    }
}
