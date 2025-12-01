package cl.duoc.lunari.api.cart.model;

/**
 * Estados posibles para un pago (Payment states)
 *
 * Flujo típico:
 * PENDIENTE → APROBADO
 *
 * Flujos alternativos:
 * - PENDIENTE → RECHAZADO (tarjeta rechazada, fondos insuficientes)
 * - PENDIENTE → EXPIRADO (usuario no completó el pago a tiempo)
 * - APROBADO → ANULADO (reembolso o reversión)
 */
public enum EstadoPago {

    /**
     * Pago iniciado, esperando confirmación de Transbank
     */
    PENDIENTE,

    /**
     * Pago aprobado por Transbank
     */
    APROBADO,

    /**
     * Pago rechazado por Transbank (fondos insuficientes, tarjeta rechazada, etc.)
     */
    RECHAZADO,

    /**
     * Pago anulado (reembolso o reversión)
     */
    ANULADO,

    /**
     * Pago expirado (timeout - usuario no completó el pago)
     */
    EXPIRADO
}
