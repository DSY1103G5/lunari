package cl.duoc.lunari.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de inicio de sesión.
 * El usuario puede autenticarse con email o username.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de inicio de sesión")
public class LoginRequest {

    @NotBlank(message = "El identificador (email o username) no puede estar vacío")
    @Schema(
        description = "Email o username del usuario",
        example = "osca.munozs@duocuc.cl",
        required = true
    )
    private String identifier; // Puede ser email o username

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Schema(
        description = "Contraseña del usuario",
        example = "password123",
        required = true
    )
    private String password;
}
