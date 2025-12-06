package cl.duoc.lunari.api.user.dto;

import cl.duoc.lunari.api.user.model.Address;
import cl.duoc.lunari.api.user.model.ClientPreferences;
import cl.duoc.lunari.api.user.model.Gaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualización de perfil de usuario
 * Todos los campos son opcionales
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud de actualización de perfil (todos los campos opcionales)")
public class UpdateProfileRequest {

    // Personal info fields
    @Schema(description = "Nombre del usuario", example = "Oscar")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "Muñoz")
    private String lastName;

    @Schema(description = "Teléfono del usuario", example = "+56912345678")
    private String phone;

    @Schema(description = "Fecha de nacimiento (YYYY-MM-DD)", example = "1995-03-15")
    private String birthdate;

    @Schema(description = "Biografía del usuario", example = "Jugador apasionado de RPGs")
    private String bio;

    @Schema(description = "URL del avatar", example = "https://example.com/avatar.jpg")
    private String avatar;

    // Complex nested objects
    @Schema(description = "Dirección de envío")
    private Address address;

    @Schema(description = "Perfil gaming")
    private Gaming gaming;

    @Schema(description = "Preferencias del cliente")
    private ClientPreferences preferences;
}
