package br.com.fiap.veti.controller.web;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte os erros de validação do formulário em "campo -> mensagem" para exibir ao lado de cada input.
 * Erros de conversão (ex.: texto no campo peso) usam as mensagens de messages.properties.
 */
final class FormErrors {

    private FormErrors() {
    }

    static Map<String, String> of(BindingResult result, MessageSource messageSource) {
        Map<String, String> erros = new LinkedHashMap<>();
        result.getFieldErrors().forEach(error -> erros.putIfAbsent(error.getField(),
                messageSource.getMessage(error, LocaleContextHolder.getLocale())));
        return erros;
    }
}
