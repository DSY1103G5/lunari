package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.model.EstadoPago;
import cl.duoc.lunari.api.cart.model.MetodoPago;
import cl.duoc.lunari.api.cart.model.Pago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para Pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDto {

    private UUID id;
    private UUID pedidoId;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private BigDecimal montoTotal;
    private String transbankToken;
    private String transbankBuyOrder;
    private String authorizationCode;
    private Integer responseCode;
    private OffsetDateTime creadoEl;
    private OffsetDateTime confirmadoEl;

    /**
     * Convierte una entidad Pago a DTO
     */
    public static PagoResponseDto fromEntity(Pago pago) {
        PagoResponseDto dto = new PagoResponseDto();
        dto.setId(pago.getId());
        dto.setPedidoId(pago.getPedido().getId());
        dto.setMetodoPago(pago.getMetodoPago());
        dto.setEstadoPago(pago.getEstadoPago());
        dto.setMontoTotal(pago.getMontoTotal());
        dto.setTransbankToken(pago.getTransbankToken());
        dto.setTransbankBuyOrder(pago.getTransbankBuyOrder());
        dto.setAuthorizationCode(pago.getAuthorizationCode());
        dto.setResponseCode(pago.getResponseCode());
        dto.setCreadoEl(pago.getCreadoEl());
        dto.setConfirmadoEl(pago.getConfirmadoEl());
        return dto;
    }
}
