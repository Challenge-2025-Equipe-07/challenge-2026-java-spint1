package br.com.fiap.veti.service;

import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.entity.PetEntity;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.exception.EmailNaoEnviadoException;
import br.com.fiap.veti.repository.ConsultaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaNotificacaoServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ConsultaNotificacaoService consultaNotificacaoService;

    private ConsultaEntity consulta() {
        var tutor = TutorEntity.builder().nome("Clara Silva").email("clara@gmail.com").build();
        var pet = PetEntity.builder().nome("Rupert").tutor(tutor).build();
        var vet = VeterinarioEntity.builder().email("ana@veti.com").build();
        return ConsultaEntity.builder().id(7L).pet(pet).veterinario(vet)
                .dataConsulta(LocalDateTime.of(2026, 11, 12, 15, 30)).build();
    }

    @Test
    void notificarTutor_deveEnviarEmailComDataFormatadaParaOTutor() {
        // Arrange
        when(consultaRepository.findByIdComPetETutor(7L)).thenReturn(Optional.of(consulta()));
        when(emailService.enviar(eq("clara@gmail.com"), eq("Lembrete de consulta - Rupert"),
                contains("12/11/2026 às 15:30"), contains("Rupert"))).thenReturn(true);

        // Act
        var resposta = consultaNotificacaoService.notificarTutor(7L);

        // Assert
        assertThat(resposta.canal()).isEqualTo("EMAIL");
        assertThat(resposta.destinatarioEmail()).isEqualTo("clara@gmail.com");
        assertThat(resposta.mensagem()).contains("Clara Silva", "Rupert", "12/11/2026 às 15:30", "ana@veti.com");
    }

    @Test
    void notificarTutor_deveIndicarModoSimuladoQuandoSmtpNaoEstaConfigurado() {
        // Arrange
        when(consultaRepository.findByIdComPetETutor(7L)).thenReturn(Optional.of(consulta()));
        when(emailService.enviar(anyString(), anyString(), anyString(), anyString())).thenReturn(false);

        // Act
        var resposta = consultaNotificacaoService.notificarTutor(7L);

        // Assert
        assertThat(resposta.canal()).isEqualTo("SIMULADO");
    }

    @Test
    void notificarTutor_devePropagarFalhaDoSmtp() {
        // Arrange
        when(consultaRepository.findByIdComPetETutor(7L)).thenReturn(Optional.of(consulta()));
        when(emailService.enviar(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new EmailNaoEnviadoException("Não foi possível enviar o e-mail para clara@gmail.com.", null));

        // Act + Assert
        assertThatThrownBy(() -> consultaNotificacaoService.notificarTutor(7L))
                .isInstanceOf(EmailNaoEnviadoException.class);
    }

    @Test
    void notificarTutor_deveFalharSemEnviarQuandoConsultaNaoExiste() {
        // Arrange
        when(consultaRepository.findByIdComPetETutor(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> consultaNotificacaoService.notificarTutor(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Consulta não encontrada");
        verify(emailService, never()).enviar(anyString(), anyString(), anyString(), anyString());
    }
}
