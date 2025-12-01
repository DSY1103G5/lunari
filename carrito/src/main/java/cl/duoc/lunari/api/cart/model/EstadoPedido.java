package cl.duoc.lunari.api.cart.model;

/**
 * Estados posibles para un pedido (Order states)
 *
 * Flujo típico:
 * CREADO → PAGO_PENDIENTE → PAGO_COMPLETADO → PROCESANDO → COMPLETADO
 *
 * Flujos alternativos:
 * - CREADO → CANCELADO (usuario cancela antes de pagar)
 * - PAGO_PENDIENTE → FALLIDO (pago rechazado o expirado)
 * - PAGO_COMPLETADO → FALLIDO (error en procesamiento post-pago)
 */
public enum EstadoPedido {

    /**
     * Pedido creado, esperando inicio de pago
     */
    CREADO,

    /**
     * Pago iniciado en Transbank, esperando confirmación del usuario
     */
    PAGO_PENDIENTE,

    /**
     * Pago confirmado exitosamente por Transbank
     */
    PAGO_COMPLETADO,

    /**
     * Procesando pedido (reducción de stock, asignación de puntos)
     */
    PROCESANDO,

    /**
     * Pedido completado exitosamente (stock reducido, puntos asignados)
     */
    COMPLETADO,

    /**
     * Pedido cancelado por usuario o sistema
     */
    CANCELADO,

    /**
     * Pedido fallido (pago rechazado o error en procesamiento)
     */
    FALLIDO
}
