package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.PetRequest;
import br.com.fiap.veti.dto.response.PetResponse;
import br.com.fiap.veti.entity.PetEntity;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.enms.Raca;
import br.com.fiap.veti.mapper.PetMapper;
import br.com.fiap.veti.repository.PetRepository;
import br.com.fiap.veti.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private PetMapper mapper;

    @InjectMocks
    private PetService petService;

    private final TutorEntity tutor = TutorEntity.builder().id(10L).nome("Ana Souza").build();

    private PetEntity pet(Long id) {
        return PetEntity.builder().id(id).nome("Rex").raca(Raca.BEAGLE.name()).peso(12.5)
                .idade(3).castrado(true).tutor(tutor).build();
    }

    private PetResponse response(Long id) {
        return new PetResponse(id, "Rex", Raca.BEAGLE, 12.5, 3, true, 10L);
    }

    @Test
    void criar_deveSalvarPetVinculadoAoTutor() {
        // Arrange
        PetRequest request = new PetRequest("Rex", Raca.BEAGLE, 12.5, 3, true, 10L);
        when(tutorRepository.findById(10L)).thenReturn(Optional.of(tutor));
        when(petRepository.save(any(PetEntity.class))).thenReturn(pet(1L));
        when(mapper.toResponse(any(PetEntity.class))).thenReturn(response(1L));

        // Act
        PetResponse result = petService.criar(request);

        // Assert
        ArgumentCaptor<PetEntity> captor = ArgumentCaptor.forClass(PetEntity.class);
        verify(petRepository).save(captor.capture());
        assertThat(captor.getValue().getTutor()).isSameAs(tutor);
        assertThat(captor.getValue().getRaca()).isEqualTo("BEAGLE");
        assertThat(captor.getValue().getPeso()).isEqualTo(12.5);
        assertThat(result.idTutor()).isEqualTo(10L);
    }

    @Test
    void criar_deveLancarNotFoundQuandoTutorNaoExiste() {
        // Arrange
        PetRequest request = new PetRequest("Rex", Raca.BEAGLE, 12.5, 3, true, 99L);
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> petService.criar(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tutor não encontrado: 99");
        verify(petRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarPetExistente() {
        // Arrange
        PetEntity entity = pet(1L);
        when(petRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response(1L));

        // Act
        PetResponse result = petService.buscarPorId(1L);

        // Assert
        assertThat(result.nome()).isEqualTo("Rex");
        assertThat(result.raca()).isEqualTo(Raca.BEAGLE);
    }

    @Test
    void buscarPorId_deveLancarNotFoundQuandoPetNaoExiste() {
        // Arrange
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> petService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pet não encontrado: 99");
    }

    @Test
    void atualizar_deveAlterarDadosETrocarTutor() {
        // Arrange
        PetEntity existente = pet(1L);
        TutorEntity novoTutor = TutorEntity.builder().id(20L).nome("Bruno").build();
        PetRequest request = new PetRequest("Thor", Raca.BOXER, 30.0, 5, false, 20L);
        when(petRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tutorRepository.findById(20L)).thenReturn(Optional.of(novoTutor));
        when(petRepository.save(existente)).thenReturn(existente);
        when(mapper.toResponse(existente)).thenReturn(new PetResponse(1L, "Thor", Raca.BOXER, 30.0, 5, false, 20L));

        // Act
        PetResponse result = petService.atualizar(1L, request);

        // Assert
        assertThat(existente.getNome()).isEqualTo("Thor");
        assertThat(existente.getRaca()).isEqualTo("BOXER");
        assertThat(existente.getPeso()).isEqualTo(30.0);
        assertThat(existente.getIdade()).isEqualTo(5);
        assertThat(existente.isCastrado()).isFalse();
        assertThat(existente.getTutor()).isSameAs(novoTutor);
        assertThat(result.idTutor()).isEqualTo(20L);
    }

    @Test
    void deletar_deveRemoverPetExistente() {
        // Arrange
        PetEntity existente = pet(1L);
        when(petRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        petService.deletar(1L);

        // Assert
        verify(petRepository).delete(existente);
    }
}
