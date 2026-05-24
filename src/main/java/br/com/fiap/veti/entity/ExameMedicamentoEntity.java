package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "TB_EXAME_MEDICAMENTO")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExameMedicamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_exame_medicamento")
    @SequenceGenerator(name = "sq_exame_medicamento", sequenceName = "SQ_EXAME_MEDICAMENTO", allocationSize = 1)
    @Column(name = "ID_PEDIDO")
    private Long id;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    @Max(value = 99, message = "Quantidade deve ter no máximo 2 dígitos")
    @Column(name = "QT_MEDICAMENTO", nullable = false)
    private Integer quantidade;

    @NotBlank(message = "Posologia é obrigatória")
    @Size(max = 1000, message = "Posologia deve ter no máximo 1000 caracteres")
    @Column(name = "DS_POSOLOGIA", nullable = false, length = 1000)
    private String posologia;

    @NotNull(message = "Exame é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_EXAME_ID_EXAME",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_EXM_MED_EXM_FK")
    )
    private ExameEntity exame;

    @NotNull(message = "Medicamento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_MEDICAMENTO_ID_MEDICAMENTO",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_EXM_MDCNT_MDCNT_FK")
    )
    private MedicamentoEntity medicamento;
}