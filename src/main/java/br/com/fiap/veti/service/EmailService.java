package br.com.fiap.veti.service;

import br.com.fiap.veti.exception.EmailNaoEnviadoException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * Envia e-mails pelo SMTP configurado em spring.mail.* (Gmail).
 * Sem usuário/senha configurados, apenas registra a mensagem no log (modo simulado).
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remetente;
    private final String remetenteNome;
    private final boolean configurado;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String usuario,
                        @Value("${spring.mail.password:}") String senha,
                        @Value("${veti.mail.remetente-nome:Clínica Veti}") String remetenteNome) {
        this.mailSender = mailSender;
        this.remetente = usuario;
        this.remetenteNome = remetenteNome;
        this.configurado = preenchido(usuario) && preenchido(senha);
        if (!configurado) {
            log.info("MAIL_USERNAME/MAIL_PASSWORD nao definidos; e-mails serao apenas registrados no log");
        }
    }

    /** Vazio ou "$(MAIL_...)" (variável ausente no Azure DevOps, que fica literal) contam como não configurado. */
    private static boolean preenchido(String valor) {
        return valor != null && !valor.isBlank() && !valor.startsWith("$(");
    }

    public boolean isConfigurado() {
        return configurado;
    }

    /** @return true se o e-mail foi enviado pelo SMTP; false se ficou só no log (modo simulado). */
    public boolean enviar(String para, String assunto, String texto, String html) {
        if (!configurado) {
            log.info("[E-MAIL SIMULADO] Para: {} | Assunto: {} | {}", para, assunto, texto);
            return false;
        }
        try {
            var mensagem = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mensagem, true, "UTF-8");
            helper.setFrom(remetente, remetenteNome);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(texto, html);
            mailSender.send(mensagem);
            log.info("E-mail enviado para {} | Assunto: {}", para, assunto);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
            throw new EmailNaoEnviadoException("Não foi possível enviar o e-mail para " + para + ".", e);
        }
    }
}
