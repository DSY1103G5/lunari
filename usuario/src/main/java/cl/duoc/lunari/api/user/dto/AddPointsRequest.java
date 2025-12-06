package cl.duoc.lunari.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para agregar puntos a un usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para agregar puntos a usuario")
public class AddPointsRequest {

    @NotNull(message = "La cantidad de puntos es obligatoria")
    @Min(value = 1, message = "La cantidad de puntos debe ser al menos 1")
    @Schema(description = "Cantidad de puntos a agregar", example = "100", required = true)
    private Long points;

    @Schema(description = "Razón de la asignación de puntos", example = "Compra realizada")
    private String reason;
}
