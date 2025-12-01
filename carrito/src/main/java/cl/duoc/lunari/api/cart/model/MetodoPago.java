package cl.duoc.lunari.api.cart.model;

/**
 * Métodos de pago disponibles
 *
 * Actualmente solo WEBPAY_PLUS está implementado
 * Los demás métodos están disponibles para implementación futura
 */
public enum MetodoPago {

    /**
     * Transbank WebPay Plus - Tarjetas de crédito y débito
     * Estado: Implementado
     */
    WEBPAY_PLUS,

    /**
     * Transferencia bancaria
     * Estado: Futuro
     */
    TRANSFERENCIA,

    /**
     * Pago en efectivo al momento de entrega
     * Estado: Futuro
     */
    EFECTIVO
}
