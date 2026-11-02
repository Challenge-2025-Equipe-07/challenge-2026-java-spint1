package br.com.fiap.veti.service;

import br.com.fiap.veti.dto.request.ConsultaRequest;
import br.com.fiap.veti.dto.response.ConsultaResponse;
import br.com.fiap.veti.entity.ConsultaEntity;
import br.com.fiap.veti.entity.PetEntity;
import br.com.fiap.veti.entity.TutorEntity;
import br.com.fiap.veti.entity.VeterinarioEntity;
import br.com.fiap.veti.mapper.ConsultaMapper;
import br.com.fiap.veti.repository.ConsultaRepository;
import br.com.fiap.veti.repository.PetRepository;
import br.com.fiap.veti.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private ConsultaMapper mapper;

    @InjectMocks
    private ConsultaService consultaService;

    private static final LocalDateTime DATA = LocalDateTime.of(2027, 3, 10, 14, 30);

    private final TutorEntity tutor = TutorEntity.builder().id(10L).nome("Ana Souza").build();
    private final PetEntity pet = PetEntity.builder().id(5L).nome("Rex").tutor(tutor).build();
    private final VeterinarioEntity vet = VeterinarioEntity.builder().id(7L).email("vet@veti.com").build();

    private ConsultaEntity consulta(Long id, LocalDateTime data) {
        return ConsultaEntity.builder().id(id).dataConsulta(data).pet(pet).veterinario(vet).build();
    }

    private ConsultaResponse response(Long id, LocalDateTime data) {
        return new ConsultaResponse(id, data, 5L, "Rex", 10L, "Ana Souza", 7L, "vet@veti.com");
    }

    @Test
    void criar_deveAgendarConsultaQuandoVeterinarioEstaLivre() {
        // Arrange
        ConsultaRequest request = new ConsultaRequest(DATA, 7L, 5L, "Vacina V10");
        Page<ConsultaEntity> agenda = new PageImpl<>(List.of(consulta(2L, DATA.plusDays(1))));
        when(petRepository.findById(5L)).thenReturn(Optional.of(pet));
        when(veterinarioRepository.findById(7L)).thenReturn(Optional.of(vet));
        when(consultaRepository.findByVeterinarioId(eq(7L), any(Pageable.class))).thenReturn(agenda);
        when(consultaRepository.save(any(ConsultaEntity.class))).thenReturn(consulta(1L, DATA));
        when(mapper.toResponse(any(ConsultaEntity.class))).thenReturn(response(1L, DATA));

        // Act
        ConsultaResponse result = consultaService.criar(request);

        // Assert
        ArgumentCaptor<ConsultaEntity> captor = ArgumentCaptor.forClass(ConsultaEntity.class);
        verify(consultaRepository).save(captor.capture());
        assertThat(captor.getValue().getDataConsulta()).isEqualTo(DATA);
        assertThat(captor.getValue().getPet()).isSameAs(pet);
        assertThat(captor.getValue().getVeterinario()).isSameAs(vet);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.tutorNome()).isEqualTo("Ana Souza");
    }

    @Test
    void criar_deveRejeitarConsultaNoMesmoHorarioDoVeterinario() {
        // Arrange
        ConsultaRequest request = new ConsultaRequest(DATA, 7L, 5L, null);
        when(petRepository.findById(5L)).thenReturn(Optional.of(pet));
        when(veterinarioRepository.findById(7L)).thenReturn(Optional.of(vet));
        when(consultaRepository.findByVeterinarioId(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(consulta(2L, DATA))));

        // Act / Assert
        assertThatThrownBy(() -> consultaService.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Veterinário já possui consulta nessa data");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void criar_deveLancarNotFoundQuandoPetNaoExiste() {
        // Arrange
        ConsultaRequest request = new ConsultaRequest(DATA, 7L, 99L, null);
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> consultaService.criar(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Pet não encontrado: 99");
        verify(consultaRepository, never()).save(any());
    }

    @Test
    void buscarPorId_deveRetornarConsultaExistente() {
        // Arrange
        ConsultaEntity entity = consulta(1L, DATA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response(1L, DATA));

        // Act
        ConsultaResponse result = consultaService.buscarPorId(1L);

        // Assert
        assertThat(result.dataConsulta()).isEqualTo(DATA);
        assertThat(result.veterinarioEmail()).isEqualTo("vet@veti.com");
    }

    @Test
    void atualizar_deveRemarcarConsulta() {
        // Arrange
        ConsultaEntity existente = consulta(1L, DATA);
        LocalDateTime novaData = DATA.plusDays(2);
        ConsultaRequest request = new ConsultaRequest(novaData, 7L, 5L, null);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(petRepository.findById(5L)).thenReturn(Optional.of(pet));
        when(veterinarioRepository.findById(7L)).thenReturn(Optional.of(vet));
        when(consultaRepository.save(existente)).thenReturn(existente);
        when(mapper.toResponse(existente)).thenReturn(response(1L, novaData));

        // Act
        ConsultaResponse result = consultaService.atualizar(1L, request);

        // Assert
        assertThat(existente.getDataConsulta()).isEqualTo(novaData);
        assertThat(result.dataConsulta()).isEqualTo(novaData);
    }

    @Test
    void deletar_deveRemoverConsultaExistente() {
        // Arrange
        ConsultaEntity existente = consulta(1L, DATA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));

        // Act
        consultaService.deletar(1L);

        // Assert
        verify(consultaRepository).delete(existente);
    }

    @Test
    void deletar_deveLancarNotFoundQuandoConsultaNaoExiste() {
        // Arrange
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> consultaService.deletar(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Consulta não encontrado: 99");
        verify(consultaRepository, never()).delete(any());
    }
}
