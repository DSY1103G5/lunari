package cl.duoc.lunari.api.cart.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un pago
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(UUID pagoId) {
        super("No se encontró el pago con ID: " + pagoId);
    }

    public PaymentNotFoundException(String token) {
        super("No se encontró el pago con token: " + token);
    }

    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
