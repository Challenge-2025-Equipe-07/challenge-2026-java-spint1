package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(
        name = "TB_RECEITA",
        indexes = {
                @Index(name = "TB_RECEITA__IDX", columnList = "TB_CONSULTA_ID_CONSULTA")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReceitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_receita")
    @SequenceGenerator(name = "sq_receita", sequenceName = "SQ_RECEITA", allocationSize = 1)
    @Column(name = "ID_RECEITA")
    private Long id;

    @NotBlank(message = "Descrição da receita é obrigatória")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "DS_RECEITA", nullable = false, length = 500)
    private String descricao;

    @NotNull(message = "Consulta é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_CONSULTA_ID_CONSULTA",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_RCT_CON_FK")
    )
    private ConsultaEntity consulta;
}