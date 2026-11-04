package br.com.fiap.veti.dto.request;

import br.com.fiap.veti.enms.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * {@code role} só é respeitado quando quem chama já é ADMIN; no auto-cadastro público a conta é sempre USER.
 */
public record RegisterRequest(@NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
                              @NotBlank(message = "Informe a senha") @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres") String password,
                              Role role) {
}
