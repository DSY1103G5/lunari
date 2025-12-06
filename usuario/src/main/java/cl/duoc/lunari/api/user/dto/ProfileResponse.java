package cl.duoc.lunari.api.user.dto;

import cl.duoc.lunari.api.user.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuesta de perfil de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Perfil completo del usuario")
public class ProfileResponse {

    @Schema(description = "ID del usuario", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Nombre de usuario", example = "omunoz")
    private String username;

    @Schema(description = "Email del usuario", example = "osca.munozs@duocuc.cl")
    private String email;

    @Schema(description = "Información personal del usuario")
    private Personal personal;

    @Schema(description = "Dirección del usuario")
    private Address address;

    @Schema(description = "Perfil gaming del usuario")
    private Gaming gaming;

    @Schema(description = "Preferencias del usuario")
    private ClientPreferences preferences;

    @Schema(description = "Estadísticas del usuario")
    private ClientStats stats;

    @Schema(description = "Cupones del usuario")
    private List<Coupon> coupons;

    @Schema(description = "Usuario activo", example = "true")
    private Boolean isActive;

    @Schema(description = "Usuario verificado", example = "false")
    private Boolean isVerified;

    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00Z")
    private String createdAt;
}
