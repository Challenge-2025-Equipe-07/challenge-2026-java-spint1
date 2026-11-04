package br.com.fiap.veti.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TutorRequest(
        @NotBlank(message = "Informe o nome") @Size(max = 300, message = "Nome deve ter no máximo 300 caracteres") String name,
        @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "Informe o telefone") @Pattern(regexp = "\\d{10,13}", message = "Telefone deve ter de 10 a 13 dígitos, só números") String phone,
        @NotBlank(message = "Informe o CPF") @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos, só números") String cpf) {
}
