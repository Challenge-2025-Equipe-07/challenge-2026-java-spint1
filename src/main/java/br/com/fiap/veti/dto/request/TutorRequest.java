package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TutorRequest(@NotBlank String name,@NotBlank String email,@NotBlank String phone,@NotBlank String cpf) {
}
