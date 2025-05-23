package cl.duoc.lunari.api.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "Usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id_usuario")
    private UUID id;

    @Column(name = "nombre_usuario", nullable = false)
    @NotBlank(message = "Nombre no puede estar vacío")
    private String firstName;

    @Column(name = "apellido_usuario", nullable = false)
    @NotBlank(message = "Apellido no puede estar vacío")
    private String lastName;

    @Column(nullable = false, unique = true)
    @Email
    @NotBlank(message = "Email no puede estar vacío")
    private String email;

    @Column(name = "contrasena", nullable = false)
    @NotBlank(message = "Contraseña no puede estar vacía")
    @Size(min = 8, max = 64, message = "Contraseña debe tener entre 8 y 64 caracteres")
    // descomentar esta validación si se necesita alguna vez
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$", message = "Contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial")
    private String password;

    @Column(name = "telefono")
    private String phone;

    @Column(name = "imagen_perfil")
    private String profileImage;

    @Column(name = "id_rol", nullable = false)
    private Integer roleId;

    @Column(name = "id_empresa")
    private UUID companyId;

    @Column(name = "activo")
    private Boolean active = true;

    @Column(name = "ultimo_login")
    private OffsetDateTime lastLogin;

    @Column(name = "verificado")
    private Boolean verified = false;

    @Column(name = "token_verificacion")
    private String verificationToken;

    @Column(name = "token_expiracion")
    private OffsetDateTime tokenExpiration;

    @Column(name = "creado_el", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "actualizado_el")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}