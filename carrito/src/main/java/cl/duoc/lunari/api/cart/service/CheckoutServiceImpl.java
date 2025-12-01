package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.CheckoutInitiateRequest;
import cl.duoc.lunari.api.cart.dto.CheckoutInitiateResponse;
import cl.duoc.lunari.api.cart.dto.TransbankInitResponse;
import cl.duoc.lunari.api.cart.model.Carrito;
import cl.duoc.lunari.api.cart.model.Pago;
import cl.duoc.lunari.api.cart.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de checkout
 * Orquesta el flujo completo de checkout
 */
@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutServiceImpl.class);

    private final CarritoService carritoService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StockReductionJob stockReductionJob;
    private final PointsAwardJob pointsAwardJob;

    @Autowired
    public CheckoutServiceImpl(
            CarritoService carritoService,
            OrderService orderService,
            PaymentService paymentService,
            StockReductionJob stockReductionJob,
            PointsAwardJob pointsAwardJob
    ) {
        this.carritoService = carritoService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.stockReductionJob = stockReductionJob;
        this.pointsAwardJob = pointsAwardJob;
    }

    @Override
    public CheckoutInitiateResponse initiateCheckout(CheckoutInitiateRequest request) {
        logger.info("Iniciando checkout para carrito: {}", request.getCarritoId());

        // 1. Obtener carrito
        Carrito carrito = carritoService.obtenerCarritoPorId(request.getCarritoId());

        // 2. Crear pedido desde carrito
        Pedido pedido = orderService.createOrderFromCart(carrito, request.getNotasCliente());

        // 3. Iniciar pago
        TransbankInitResponse paymentResponse = paymentService.initiatePayment(pedido, request.getReturnUrl());

        // 4. Marcar carrito como procesado
        carritoService.markCartProcessed(carrito.getId(), pedido.getNumeroPedido());

        logger.info("Checkout iniciado - Pedido: {}, Token: {}",
                pedido.getNumeroPedido(), paymentResponse.getToken());

        return new CheckoutInitiateResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                paymentResponse.getUrl(),
                paymentResponse.getToken()
        );
    }

    @Override
    public Pedido confirmCheckout(String token) {
        logger.info("Confirmando checkout con token: {}", token);

        // 1. Confirmar pago (esto actualiza el estado del pago y pedido)
        Pago pago = paymentService.confirmPayment(token);

        // 2. Si el pago fue aprobado, procesar acciones posteriores
        if (pago.estaAprobado()) {
            logger.info("Pago aprobado - Iniciando procesamiento posterior");

            // Aquí llamaremos a los jobs de forma asíncrona
            // Los jobs se encargarán de reducir stock y asignar puntos
            processPostPaymentActions(pago.getPedido());
        }

        return pago.getPedido();
    }

    /**
     * Procesa acciones posteriores al pago aprobado de forma asíncrona
     */
    @Async("taskExecutor")
    public void processPostPaymentActions(Pedido pedido) {
        logger.info("Procesando acciones post-pago para pedido: {}", pedido.getNumeroPedido());

        try {
            // 1. Reducir stock en inventario (async)
            stockReductionJob.execute(pedido);

            // 2. Asignar puntos al usuario (async)
            pointsAwardJob.execute(pedido);

            // 3. Marcar pedido como COMPLETADO
            // Nota: Esto se hace de forma optimista. Si los jobs fallan, quedan registrados
            // en los logs para reconciliación manual
            orderService.markOrderComplete(pedido.getId(), pedido.getTotalPuntosGanados());

            logger.info("Acciones post-pago completadas para pedido: {}", pedido.getNumeroPedido());

        } catch (Exception e) {
            logger.error("Error en acciones post-pago para pedido: {}", pedido.getNumeroPedido(), e);
            // En caso de error, el pedido queda en estado PAGO_COMPLETADO
            // Los jobs de reconciliación pueden procesarlo posteriormente
        }
    }
}
