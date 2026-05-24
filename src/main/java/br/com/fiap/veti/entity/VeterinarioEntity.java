package br.com.fiap.veti.entity;

import br.com.fiap.veti.enms.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(
        name = "TB_VETERINARIO",
        uniqueConstraints = {
                @UniqueConstraint(name = "TB_VETERINARIO_DS_EMAIL_UN", columnNames = "DS_EMAIL")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VeterinarioEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_veterinario")
    @SequenceGenerator(name = "sq_veterinario", sequenceName = "SQ_VETERINARIO", allocationSize = 1)
    @Column(name = "ID_VETERINARIO")
    private Long id;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 320, message = "E-mail deve ter no máximo 320 caracteres")
    @Column(name = "DS_EMAIL", nullable = false, length = 320, unique = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = 5000, message = "Senha deve ter no máximo 5000 caracteres")
    @Column(name = "DS_PASSWORD", nullable = false, length = 5000)
    private String password;

    @NotBlank(message = "Role e obrigatoria")
    @Column(name = "DS_ROLE")
    private String role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (Role.ADMIN.equals(this.role)) {
            return List.of(new SimpleGrantedAuthority("ADMIN"), new SimpleGrantedAuthority("USER"));
        } else {
            return List.of(new SimpleGrantedAuthority("USER"));
        }
    }

    @Override
    public String getUsername() {
        return this.email;
    }


}