package cl.duoc.lunari.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de respuesta al iniciar checkout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutInitiateResponse {

    private UUID orderId;
    private String numeroOrden;
    private String paymentUrl;
    private String transbankToken;

    public CheckoutInitiateResponse(UUID orderId, String numeroOrden, String paymentUrl) {
        this.orderId = orderId;
        this.numeroOrden = numeroOrden;
        this.paymentUrl = paymentUrl;
    }
}
