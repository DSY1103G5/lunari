package cl.duoc.lunari.api.cart.exception;

import cl.duoc.lunari.api.cart.model.EstadoPedido;

/**
 * Excepción lanzada cuando se intenta una operación en un estado de pedido inválido
 */
public class InvalidOrderStateException extends RuntimeException {

    private final EstadoPedido estadoActual;
    private final EstadoPedido estadoRequerido;

    public InvalidOrderStateException(EstadoPedido estadoActual, EstadoPedido estadoRequerido) {
        super(String.format("El pedido está en estado %s, pero se requiere estado %s para esta operación",
                estadoActual, estadoRequerido));
        this.estadoActual = estadoActual;
        this.estadoRequerido = estadoRequerido;
    }

    public InvalidOrderStateException(String message) {
        super(message);
        this.estadoActual = null;
        this.estadoRequerido = null;
    }

    public EstadoPedido getEstadoActual() {
        return estadoActual;
    }

    public EstadoPedido getEstadoRequerido() {
        return estadoRequerido;
    }
}
