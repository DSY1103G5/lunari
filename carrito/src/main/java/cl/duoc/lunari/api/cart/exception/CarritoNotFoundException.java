package cl.duoc.lunari.api.cart.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un carrito
 */
public class CarritoNotFoundException extends RuntimeException {

    public CarritoNotFoundException(UUID carritoId) {
        super("Carrito no encontrado con ID: " + carritoId);
    }

    public CarritoNotFoundException(String message) {
        super(message);
    }
}
