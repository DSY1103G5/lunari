package cl.duoc.lunari.api.cart.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un pedido
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID orderId) {
        super("No se encontró el pedido con ID: " + orderId);
    }

    public OrderNotFoundException(String numeroOrden) {
        super("No se encontró el pedido con número: " + numeroOrden);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
