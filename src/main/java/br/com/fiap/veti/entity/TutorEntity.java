package br.com.fiap.veti.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
        name = "TB_TUTOR",
        uniqueConstraints = {
                @UniqueConstraint(name = "TB_TUTOR_DS_CPF_UN", columnNames = "DS_CPF"),
                @UniqueConstraint(name = "TB_TUTOR_DS_EMAIL_UN", columnNames = "DS_EMAIL")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_tutor")
    @SequenceGenerator(name = "sq_tutor", sequenceName = "SQ_TUTOR", allocationSize = 1)
    @Column(name = "ID_TUTOR")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 300, message = "Nome deve ter no máximo 300 caracteres")
    @Column(name = "NM_TUTOR", nullable = false, length = 300)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 caracteres")
    @Column(name = "DS_CPF", nullable = false, length = 11, unique = true)
    private String cpf;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 320, message = "E-mail deve ter no máximo 320 caracteres")
    @Column(name = "DS_EMAIL", nullable = false, length = 320, unique = true)
    private String email;

    @Size(max = 13, message = "Telefone deve ter no máximo 13 caracteres")
    @Column(name = "DS_TELEFONE", length = 13)
    private String telefone;
}