package cl.duoc.lunari.api.cart.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta procesar un carrito vacío
 */
public class EmptyCartException extends RuntimeException {

    private final UUID carritoId;

    public EmptyCartException(UUID carritoId) {
        super("El carrito con ID " + carritoId + " está vacío. No se puede procesar.");
        this.carritoId = carritoId;
    }

    public EmptyCartException(String message) {
        super(message);
        this.carritoId = null;
    }

    public UUID getCarritoId() {
        return carritoId;
    }
}
