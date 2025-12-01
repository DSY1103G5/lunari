package cl.duoc.lunari.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de respuesta de Transbank al confirmar una transacción
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransbankConfirmResponse {

    private String buyOrder;
    private String sessionId;
    private BigDecimal amount;
    private String authorizationCode;
    private String paymentTypeCode;
    private Integer responseCode;
    private OffsetDateTime transactionDate;
    private String cardNumber;

    /**
     * Verifica si el pago fue aprobado
     */
    public boolean isApproved() {
        return responseCode != null && responseCode == 0;
    }
}
