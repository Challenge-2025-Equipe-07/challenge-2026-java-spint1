package br.com.fiap.veti.dto.response;

import br.com.fiap.veti.enms.Raca;

public record PetResponse(Long id, String nome, Raca raca, Double peso, int idade, boolean castrado, Long idTutor) {
}
