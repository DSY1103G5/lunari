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
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "Usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id_usuario")
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Column(name = "nombre_usuario", nullable = false)
    @NotBlank(message = "Nombre no puede estar vacío")
    @Schema(example = "Angelo")
    private String firstName;

    @Column(name = "apellido_usuario", nullable = false)
    @NotBlank(message = "Apellido no puede estar vacío")
    @Schema(example = "Millan")
    private String lastName;

    @Column(nullable = false, unique = true)
    @Email
    @NotBlank(message = "Email no puede estar vacío")
    @Schema(example = "ang.millan@duocuc.com")
    private String email;

    @Column(name = "contrasena", nullable = false)
    @NotBlank(message = "Contraseña no puede estar vacía")
    @Size(min = 8, max = 64, message = "Contraseña debe tener entre 8 y 64 caracteres")
    @Schema(example = "123456789")
    private String password;
    // descomentar esta validación si se necesita alguna vez
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,64}$", message = "Contraseña debe contener al menos una letra mayúscula, una letra minúscula, un número y un carácter especial")

    @Column(name = "telefono")
    @Schema(example = "+56900000000")
    private String phone;

    @Column(name = "imagen_perfil")
    @Schema(example = "https://imagenes.ejemplo.com")
    private String profileImage;

    @Column(name = "id_rol", nullable = false)
    @Schema(example = "1")
    private Integer roleId;

    @Column(name = "id_empresa")
    @Schema(example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID companyId;

    @Column(name = "activo")
    @Schema(example = "true")
    private Boolean active = true;

    @Column(name = "ultimo_login")
    @Schema(example = "2025-07-04")
    private OffsetDateTime lastLogin;

    @Column(name = "verificado")
    @Schema(example = "true")
    private Boolean verified = false;

    @Column(name = "token_verificacion")
    @Schema(example = "abc123-verification-token")
    private String verificationToken;

    @Column(name = "token_expiracion")
    @Schema(example = "2025-07-05")
    private OffsetDateTime tokenExpiration;

    @Column(name = "creado_el", updatable = false)
    @Schema(example = "2025-07-01")
    private OffsetDateTime createdAt;

    @Column(name = "actualizado_el")
    @Schema(example = "2025-07-04")
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
