package cl.duoc.lunari.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registro de nuevos usuarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de registro de nuevo usuario")
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Schema(description = "Nombre de usuario", example = "omunoz", required = true)
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Schema(description = "Email del usuario", example = "osca.munozs@duocuc.cl", required = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "SecurePassword123", required = true)
    private String password;

    @Schema(description = "Nombre del usuario", example = "Oscar")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Muñoz")
    private String lastName;

    @Schema(description = "Teléfono del usuario", example = "+56912345678")
    private String phone;
}
