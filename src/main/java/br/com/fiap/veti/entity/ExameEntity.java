package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "TB_EXAME")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_exame")
    @SequenceGenerator(name = "sq_exame", sequenceName = "SQ_EXAME", allocationSize = 1)
    @Column(name = "ID_EXAME")
    private Long id;

    @NotBlank(message = "Descrição do exame é obrigatória")
    @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
    @Column(name = "DS_EXAME", nullable = false, length = 100)
    private String descricao;

    @Size(max = 500, message = "URL deve ter no máximo 500 caracteres")
    @Column(name = "DS_URL", length = 500)
    private String url;

    @NotNull(message = "Consulta é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_CONSULTA_ID_CONSULTA",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_EXM_TB_CON_FK")
    )
    private ConsultaEntity consulta;
}