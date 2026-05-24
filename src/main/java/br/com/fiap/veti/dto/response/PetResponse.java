package br.com.fiap.veti.dto.response;

import br.com.fiap.veti.enms.Raca;

public record PetResponse(long id, String name, Raca raca, Double peso, int idade, boolean castrado, long idTutor) {
}
