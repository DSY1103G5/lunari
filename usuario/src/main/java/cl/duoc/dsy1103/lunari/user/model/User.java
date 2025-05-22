package cl.duoc.dsy1103.lunari.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Nombre no puede estar vacío")
    private String name;

    @Column(nullable = false, unique = true)
    @Email
    @NotBlank(message = "Email no puede estar vacío")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Contraseña no puede estar vacía")
    @Size(min = 8, max = 64, message = "Contraseña debe tener entre 8 y 64 caracteres")
    // descomentar esta validación si se necesita alguna vez
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$", message = "Contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;


}
