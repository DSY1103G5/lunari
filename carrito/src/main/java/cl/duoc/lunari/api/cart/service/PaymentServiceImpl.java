package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.TransbankConfirmResponse;
import cl.duoc.lunari.api.cart.dto.TransbankInitResponse;
import cl.duoc.lunari.api.cart.exception.PaymentFailedException;
import cl.duoc.lunari.api.cart.exception.PaymentNotFoundException;
import cl.duoc.lunari.api.cart.model.EstadoPago;
import cl.duoc.lunari.api.cart.model.EstadoPedido;
import cl.duoc.lunari.api.cart.model.MetodoPago;
import cl.duoc.lunari.api.cart.model.Pago;
import cl.duoc.lunari.api.cart.model.Pedido;
import cl.duoc.lunari.api.cart.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del servicio de gestión de pagos
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PagoRepository pagoRepository;
    private final TransbankService transbankService;
    private final OrderService orderService;

    @Autowired
    public PaymentServiceImpl(
            PagoRepository pagoRepository,
            TransbankService transbankService,
            OrderService orderService
    ) {
        this.pagoRepository = pagoRepository;
        this.transbankService = transbankService;
        this.orderService = orderService;
    }

    @Override
    public TransbankInitResponse initiatePayment(Pedido pedido, String returnUrl) {
        logger.info("Iniciando pago para pedido: {}", pedido.getNumeroPedido());

        // Generar IDs únicos para Transbank
        String buyOrder = "BUY-" + pedido.getNumeroPedido();
        String sessionId = "SES-" + pedido.getId().toString();

        // Crear transacción en Transbank
        TransbankInitResponse transbankResponse = transbankService.createTransaction(
                pedido.getTotalProductos(),
                buyOrder,
                sessionId,
                returnUrl
        );

        // Crear registro de pago
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(MetodoPago.WEBPAY_PLUS);
        pago.setEstadoPago(EstadoPago.PENDIENTE);
        pago.setMontoTotal(pedido.getTotalProductos());
        pago.setTransbankToken(transbankResponse.getToken());
        pago.setTransbankBuyOrder(buyOrder);
        pago.setTransbankSessionId(sessionId);
        pago.setPaymentUrl(transbankResponse.getUrl());

        pagoRepository.save(pago);

        // Actualizar estado del pedido
        orderService.updateOrderStatus(pedido.getId(), EstadoPedido.PAGO_PENDIENTE);

        logger.info("Pago iniciado exitosamente - Token: {}", transbankResponse.getToken());

        return transbankResponse;
    }

    @Override
    public Pago confirmPayment(String token) {
        logger.info("Confirmando pago con token: {}", token);

        // Obtener pago por token
        Pago pago = getPaymentByToken(token);

        // Confirmar transacción en Transbank
        TransbankConfirmResponse confirmResponse = transbankService.confirmTransaction(token);

        // Procesar según resultado
        if (confirmResponse.isApproved()) {
            processApprovedPayment(pago, confirmResponse);
        } else {
            processRejectedPayment(pago, confirmResponse);
        }

        return pago;
    }

    @Override
    @Transactional(readOnly = true)
    public Pago getPaymentById(UUID pagoId) {
        return pagoRepository.findById(pagoId)
                .orElseThrow(() -> new PaymentNotFoundException(pagoId));
    }

    @Override
    @Transactional(readOnly = true)
    public Pago getPaymentByOrderId(UUID pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new PaymentNotFoundException("No se encontró pago para pedido: " + pedidoId, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Pago getPaymentByToken(String token) {
        return pagoRepository.findByTransbankToken(token)
                .orElseThrow(() -> new PaymentNotFoundException(token));
    }

    @Override
    public void processApprovedPayment(Pago pago, TransbankConfirmResponse confirmResponse) {
        logger.info("Procesando pago aprobado - Buy Order: {}, Authorization: {}",
                confirmResponse.getBuyOrder(), confirmResponse.getAuthorizationCode());

        // Actualizar pago
        pago.marcarComoAprobado(
                confirmResponse.getAuthorizationCode(),
                confirmResponse.getResponseCode()
        );
        pagoRepository.save(pago);

        // Actualizar pedido
        orderService.updateOrderStatus(pago.getPedido().getId(), EstadoPedido.PAGO_COMPLETADO);

        logger.info("Pago aprobado procesado exitosamente");
    }

    @Override
    public void processRejectedPayment(Pago pago, TransbankConfirmResponse confirmResponse) {
        logger.warn("Procesando pago rechazado - Buy Order: {}, Response Code: {}",
                confirmResponse.getBuyOrder(), confirmResponse.getResponseCode());

        // Actualizar pago
        pago.marcarComoRechazado(confirmResponse.getResponseCode());
        pagoRepository.save(pago);

        // Actualizar pedido
        orderService.updateOrderStatus(pago.getPedido().getId(), EstadoPedido.FALLIDO);

        String errorMessage = "Pago rechazado por Transbank. Código: " + confirmResponse.getResponseCode();
        logger.error(errorMessage);
        throw new PaymentFailedException(errorMessage, confirmResponse.getResponseCode());
    }
}
