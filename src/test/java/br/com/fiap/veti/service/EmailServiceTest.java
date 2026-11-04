package br.com.fiap.veti.service;

import br.com.fiap.veti.exception.EmailNaoEnviadoException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MimeMessage mensagemVazia() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    void enviar_deveMandarPeloSmtpComRemetenteDaContaGmail() throws Exception {
        // Arrange
        var service = new EmailService(mailSender, "clinica@gmail.com", "senha-de-app", "Clínica Veti");
        when(mailSender.createMimeMessage()).thenReturn(mensagemVazia());

        // Act
        boolean enviado = service.enviar("clara@gmail.com", "Lembrete", "texto", "<p>html</p>");

        // Assert
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(enviado).isTrue();
        assertThat(captor.getValue().getFrom()[0].toString()).contains("clinica@gmail.com");
        assertThat(captor.getValue().getAllRecipients()[0].toString()).isEqualTo("clara@gmail.com");
        assertThat(captor.getValue().getSubject()).isEqualTo("Lembrete");
    }

    @Test
    void enviar_deveApenasRegistrarNoLogQuandoSmtpNaoEstaConfigurado() {
        // Arrange
        var service = new EmailService(mailSender, "", "", "Clínica Veti");

        // Act
        boolean enviado = service.enviar("clara@gmail.com", "Lembrete", "texto", "<p>html</p>");

        // Assert
        assertThat(enviado).isFalse();
        assertThat(service.isConfigurado()).isFalse();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void enviar_deveConverterFalhaDoSmtpEmEmailNaoEnviado() {
        // Arrange
        var service = new EmailService(mailSender, "clinica@gmail.com", "senha-errada", "Clínica Veti");
        when(mailSender.createMimeMessage()).thenReturn(mensagemVazia());
        doThrow(new MailSendException("535 Authentication failed")).when(mailSender).send(any(MimeMessage.class));

        // Act + Assert
        assertThatThrownBy(() -> service.enviar("clara@gmail.com", "Lembrete", "texto", "<p>html</p>"))
                .isInstanceOf(EmailNaoEnviadoException.class)
                .hasMessage("Não foi possível enviar o e-mail para clara@gmail.com.");
    }
}
