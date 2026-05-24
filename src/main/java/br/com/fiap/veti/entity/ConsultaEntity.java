package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "TB_CONSULTA",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "TB_CON_DT_VET_UN",
                        columnNames = {"DT_CONSULTA", "TB_VETERINARIO_ID_VETERINARIO"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_consulta")
    @SequenceGenerator(name = "sq_consulta", sequenceName = "SQ_CONSULTA", allocationSize = 1)
    @Column(name = "ID_CONSULTA")
    private Long id;

    @NotNull(message = "Data da consulta é obrigatória")
    @Column(name = "DT_CONSULTA", nullable = false)
    private LocalDateTime dataConsulta;

    @NotNull(message = "Pet é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_PET_ID_PET",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_CONSULTA_TB_PET_FK")
    )
    private PetEntity pet;

    @NotNull(message = "Veterinário é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "TB_VETERINARIO_ID_VETERINARIO",
            nullable = false,
            foreignKey = @ForeignKey(name = "TB_CON_TB_VET_FK")
    )
    private VeterinarioEntity veterinario;
}