package cl.duoc.lunari.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para canjear puntos por cupones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para canjear puntos por cupón")
public class RedeemPointsRequest {

    @NotNull(message = "La cantidad de puntos es obligatoria")
    @Min(value = 100, message = "Se requieren al menos 100 puntos para canjear")
    @Schema(description = "Cantidad de puntos a canjear (mínimo 100)", example = "500", required = true)
    private Long pointsToRedeem;
}
