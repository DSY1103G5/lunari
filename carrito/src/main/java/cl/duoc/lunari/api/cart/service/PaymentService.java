package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.TransbankConfirmResponse;
import cl.duoc.lunari.api.cart.dto.TransbankInitResponse;
import cl.duoc.lunari.api.cart.model.Pago;
import cl.duoc.lunari.api.cart.model.Pedido;

import java.util.UUID;

/**
 * Servicio para gestión de pagos
 */
public interface PaymentService {

    /**
     * Inicia un pago para un pedido
     *
     * @param pedido Pedido a pagar
     * @param returnUrl URL de retorno después del pago
     * @return Respuesta de Transbank con token y URL de pago
     */
    TransbankInitResponse initiatePayment(Pedido pedido, String returnUrl);

    /**
     * Confirma un pago usando el token de Transbank
     *
     * @param token Token de la transacción
     * @return Pago confirmado
     */
    Pago confirmPayment(String token);

    /**
     * Obtiene un pago por su ID
     *
     * @param pagoId ID del pago
     * @return Pago encontrado
     */
    Pago getPaymentById(UUID pagoId);

    /**
     * Obtiene un pago por ID de pedido
     *
     * @param pedidoId ID del pedido
     * @return Pago encontrado
     */
    Pago getPaymentByOrderId(UUID pedidoId);

    /**
     * Obtiene un pago por token de Transbank
     *
     * @param token Token de Transbank
     * @return Pago encontrado
     */
    Pago getPaymentByToken(String token);

    /**
     * Procesa un pago aprobado
     * Actualiza el estado del pedido y dispara acciones posteriores
     *
     * @param pago Pago aprobado
     * @param confirmResponse Respuesta de confirmación de Transbank
     */
    void processApprovedPayment(Pago pago, TransbankConfirmResponse confirmResponse);

    /**
     * Procesa un pago rechazado
     *
     * @param pago Pago rechazado
     * @param confirmResponse Respuesta de confirmación de Transbank
     */
    void processRejectedPayment(Pago pago, TransbankConfirmResponse confirmResponse);
}
