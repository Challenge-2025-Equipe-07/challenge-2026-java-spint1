package br.com.fiap.veti.dto.request;

import br.com.fiap.veti.enms.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(@NotBlank String email, @NotBlank String password, @NotNull Role role) {

}
