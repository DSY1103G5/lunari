package cl.duoc.lunari.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de cupón creado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de cupón canjeado")
public class CouponResponse {

    @Schema(description = "ID del cupón", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Código del cupón", example = "GOL-A1B2C3D4-5678")
    private String code;

    @Schema(description = "Descripción del cupón", example = "Canjeaste 500 puntos como miembro Gold")
    private String description;

    @Schema(description = "Tipo de cupón (fixed o percentage)", example = "fixed")
    private String type;

    @Schema(description = "Valor del cupón", example = "10.0")
    private Double value;

    @Schema(description = "Compra mínima requerida", example = "0.0")
    private Double minPurchase;

    @Schema(description = "Fecha de expiración (YYYY-MM-DD)", example = "2024-06-15")
    private String expiresAt;

    @Schema(description = "Mensaje de confirmación", example = "¡Cupón creado exitosamente!")
    private String message;
}
