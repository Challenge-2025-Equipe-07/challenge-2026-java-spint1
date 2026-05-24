package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "TB_MEDICAMENTO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_medicamento")
    @SequenceGenerator(name = "sq_medicamento", sequenceName = "SQ_MEDICAMENTO", allocationSize = 1)
    @Column(name = "ID_MEDICAMENTO")
    private Long id;

    @NotBlank(message = "Nome do medicamento é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(name = "NM_MEDICAMENTO", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 400, message = "Descrição deve ter no máximo 400 caracteres")
    @Column(name = "DS_MEDICAMENTO", nullable = false, length = 400)
    private String descricao;
}