package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.CheckoutInitiateRequest;
import cl.duoc.lunari.api.cart.dto.CheckoutInitiateResponse;
import cl.duoc.lunari.api.cart.model.Pedido;

/**
 * Servicio para gestionar el proceso de checkout completo
 * Orquesta la creación de pedidos, pagos, y acciones posteriores
 */
public interface CheckoutService {

    /**
     * Inicia el proceso de checkout
     * Crea un pedido, inicia el pago, y devuelve la URL de pago
     *
     * @param request Solicitud de checkout con ID de carrito y URL de retorno
     * @return Respuesta con URL de pago de Transbank
     */
    CheckoutInitiateResponse initiateCheckout(CheckoutInitiateRequest request);

    /**
     * Confirma el pago y completa el checkout
     * Procesa el pago, actualiza stock, asigna puntos
     *
     * @param token Token de Transbank
     * @return Pedido completado
     */
    Pedido confirmCheckout(String token);
}
