package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email,@NotBlank String password) {
}
