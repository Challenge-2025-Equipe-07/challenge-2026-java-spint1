package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.TutorRequest;
import br.com.fiap.veti.dto.response.TutorResponse;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.mapper.TutorMapper;
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
class TutorServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private TutorMapper mapper;

    @InjectMocks
    private TutorService tutorService;

    private final TutorRequest request = new TutorRequest("Ana Souza", "ana@email.com", "11999999999", "12345678901");

    private TutorEntity tutor(Long id) {
        return TutorEntity.builder().id(id).nome("Ana Souza").email("ana@email.com")
                .telefone("11999999999").cpf("12345678901").build();
    }

    private TutorResponse response(Long id) {
        return new TutorResponse(id, "Ana Souza", "ana@email.com", "11999999999", "12345678901");
    }

    @Test
    void criar_deveSalvarTutorQuandoCpfEEmailSaoNovos() {
        // Arrange
        when(tutorRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());
        when(tutorRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(tutorRepository.save(any(TutorEntity.class))).thenReturn(tutor(1L));
        when(mapper.toResponse(any(TutorEntity.class))).thenReturn(response(1L));

        // Act
        TutorResponse result = tutorService.criar(request);

        // Assert
        ArgumentCaptor<TutorEntity> captor = ArgumentCaptor.forClass(TutorEntity.class);
        verify(tutorRepository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Ana Souza");
        assertThat(captor.getValue().getCpf()).isEqualTo("12345678901");
        assertThat(captor.getValue().getTelefone()).isEqualTo("11999999999");
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void criar_deveLancarExcecaoQuandoCpfJaCadastrado() {
        // Arrange
        when(tutorRepository.findByCpf(request.cpf())).thenReturn(Optional.of(tutor(9L)));

        // Act / Assert
        assertThatThrownBy(() -> tutorService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF já cadastrado");
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void criar_deveLancarExcecaoQuandoEmailJaCadastrado() {
        // Arrange
        when(tutorRepository.findByCpf(request.cpf())).thenReturn(Optional.empty());
        when(tutorRepository.findByEmail(request.email())).thenReturn(Optional.of(tutor(9L)));

        // Act / Assert
        assertThatThrownBy(() -> tutorService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já cadastrado");
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarTutorExistente() {
        // Arrange
        TutorEntity entity = tutor(1L);
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response(1L));

        // Act
        TutorResponse result = tutorService.buscarPorId(1L);

        // Assert
        assertThat(result).isEqualTo(response(1L));
    }

    @Test
    void buscarPorId_deveLancarNotFoundQuandoTutorNaoExiste() {
        // Arrange
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> tutorService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tutor não encontrado: 99");
    }

    @Test
    void atualizar_deveAlterarDadosDoTutor() {
        // Arrange
        TutorEntity existente = tutor(1L);
        TutorRequest novo = new TutorRequest("Ana Lima", "ana.lima@email.com", "11888888888", "10987654321");
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(tutorRepository.save(existente)).thenReturn(existente);
        when(mapper.toResponse(existente))
                .thenReturn(new TutorResponse(1L, "Ana Lima", "ana.lima@email.com", "11888888888", "10987654321"));

        // Act
        TutorResponse result = tutorService.atualizar(1L, novo);

        // Assert
        assertThat(existente.getNome()).isEqualTo("Ana Lima");
        assertThat(existente.getEmail()).isEqualTo("ana.lima@email.com");
        assertThat(existente.getTelefone()).isEqualTo("11888888888");
        assertThat(existente.getCpf()).isEqualTo("10987654321");
        assertThat(result.name()).isEqualTo("Ana Lima");
    }

    @Test
    void deletar_deveRemoverTutorExistente() {
        // Arrange
        TutorEntity existente = tutor(1L);
        when(tutorRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        tutorService.deletar(1L);

        // Assert
        verify(tutorRepository).delete(existente);
    }

    @Test
    void deletar_deveLancarNotFoundQuandoTutorNaoExiste() {
        // Arrange
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> tutorService.deletar(99L)).isInstanceOf(EntityNotFoundException.class);
        verify(tutorRepository, never()).delete(any());
    }
}
