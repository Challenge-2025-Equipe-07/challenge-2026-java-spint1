package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "TB_PET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_pet")
    @SequenceGenerator(name = "sq_pet", sequenceName = "SQ_PET", allocationSize = 1)
    @Column(name = "ID_PET")
    private Long id;

    @NotBlank(message = "Nome do pet é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(name = "NM_PET", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Raça é obrigatória")
    @Size(max = 100, message = "Raça deve ter no máximo 100 caracteres")
    @Column(name = "DS_RACA", nullable = false, length = 100)
    private String raca;

    @NotNull(message = "Peso é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser maior que zero")
    @Column(name = "DS_PESO", nullable = false, precision = 5)
    private Double peso;

    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 99, message = "Idade deve ter no máximo 2 dígitos")
    @Column(name = "DS_IDADE", nullable = false)
    private Integer idade;

    @NotNull(message = "Tutor é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_TUTOR_ID_TUTOR",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_PET_TB_TUTOR_FK")
    )
    private TutorEntity tutor;
}