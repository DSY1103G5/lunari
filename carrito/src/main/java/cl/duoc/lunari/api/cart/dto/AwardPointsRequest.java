package cl.duoc.lunari.api.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para solicitar asignación de puntos en el servicio de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AwardPointsRequest {

    @NotNull(message = "ID de usuario no puede estar vacío")
    private UUID usuarioId;

    @NotNull(message = "Puntos no pueden estar vacíos")
    @Min(value = 0, message = "Puntos deben ser mayor o igual a 0")
    private Integer puntos;

    private String razon;

    private String referencia;

    public AwardPointsRequest(UUID usuarioId, Integer puntos) {
        this.usuarioId = usuarioId;
        this.puntos = puntos;
        this.razon = "Compra realizada";
    }
}
