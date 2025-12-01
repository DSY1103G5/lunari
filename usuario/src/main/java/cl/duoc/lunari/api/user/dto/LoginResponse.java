package cl.duoc.lunari.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de inicio de sesión exitoso.
 * Incluye información del usuario y un token de sesión.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de inicio de sesión exitoso")
public class LoginResponse {

    @Schema(description = "ID del usuario", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "Username del usuario", example = "omunoz")
    private String username;

    @Schema(description = "Email del usuario", example = "osca.munozs@duocuc.cl")
    private String email;

    @Schema(description = "Nombre completo del usuario", example = "Oscar Muñoz")
    private String fullName;

    @Schema(description = "Rol del usuario", example = "CLIENT")
    private String role;

    @Schema(description = "ID del rol", example = "3")
    private Integer roleId;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean isActive;

    @Schema(description = "Indica si el usuario está verificado", example = "true")
    private Boolean isVerified;

    @Schema(description = "Token de sesión (simple session token)")
    private String sessionToken;

    @Schema(description = "Nivel del usuario", example = "Gold")
    private String level;

    @Schema(description = "Puntos del usuario", example = "1500")
    private Long points;

    @Schema(description = "Mensaje de bienvenida", example = "¡Bienvenido de vuelta!")
    private String message;
}
