package br.com.fiap.veti.exception;

/** O servidor SMTP recusou ou não respondeu ao envio do e-mail. */
public class EmailNaoEnviadoException extends RuntimeException {

    public EmailNaoEnviadoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
