package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.exception.EmptyCartException;
import cl.duoc.lunari.api.cart.exception.InvalidOrderStateException;
import cl.duoc.lunari.api.cart.exception.OrderNotFoundException;
import cl.duoc.lunari.api.cart.model.*;
import cl.duoc.lunari.api.cart.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación del servicio de gestión de pedidos
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final AtomicLong orderCounter = new AtomicLong(1);

    private final PedidoRepository pedidoRepository;

    @Autowired
    public OrderServiceImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public Pedido createOrderFromCart(Carrito carrito, String notasCliente) {
        logger.info("Creando pedido desde carrito ID: {}", carrito.getId());

        // Validar que el carrito tenga items
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new EmptyCartException(carrito.getId());
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(generateOrderNumber());
        pedido.setCarritoId(carrito.getId());
        pedido.setUsuarioId(carrito.getUsuarioId());
        pedido.setEstadoPedido(EstadoPedido.CREADO);
        pedido.setNotasCliente(notasCliente != null ? notasCliente : carrito.getNotasCliente());

        // Convertir items del carrito a items del pedido
        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem carritoItem : carrito.getItems()) {
            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.setProductoId(carritoItem.getServicioId().longValue());
            pedidoItem.setCodigoProducto("PROD-" + carritoItem.getServicioId()); // Idealmente obtener del servicio
            pedidoItem.setNombreProducto("Producto " + carritoItem.getServicioId()); // Idealmente obtener del servicio
            pedidoItem.setCantidad(carritoItem.getCantidad());
            pedidoItem.setPrecioUnitario(carritoItem.getPrecioUnitario());
            pedidoItem.setSubtotal(carritoItem.getSubtotal());

            pedido.agregarItem(pedidoItem);
            total = total.add(carritoItem.getSubtotal());
        }

        pedido.setTotalProductos(total);
        pedido.setTotalPuntosGanados(pedido.calcularPuntosAGanar());

        Pedido savedPedido = pedidoRepository.save(pedido);
        logger.info("Pedido creado exitosamente - Número: {}, Total: {}",
                savedPedido.getNumeroPedido(), savedPedido.getTotalProductos());

        return savedPedido;
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido getOrderById(UUID orderId) {
        return pedidoRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido getOrderByNumero(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new OrderNotFoundException(numeroPedido));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> getOrdersByUsuario(UUID usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByCreadoElDesc(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> getOrdersByUsuarioAndEstado(UUID usuarioId, EstadoPedido estado) {
        return pedidoRepository.findByUsuarioIdAndEstadoPedidoOrderByCreadoElDesc(usuarioId, estado);
    }

    @Override
    public Pedido updateOrderStatus(UUID orderId, EstadoPedido nuevoEstado) {
        logger.info("Actualizando estado de pedido {} a {}", orderId, nuevoEstado);

        Pedido pedido = getOrderById(orderId);
        pedido.setEstadoPedido(nuevoEstado);

        return pedidoRepository.save(pedido);
    }

    @Override
    public Pedido markOrderComplete(UUID orderId, Integer puntosGanados) {
        logger.info("Marcando pedido {} como completado con {} puntos", orderId, puntosGanados);

        Pedido pedido = getOrderById(orderId);

        if (pedido.getEstadoPedido() != EstadoPedido.PAGO_COMPLETADO &&
                pedido.getEstadoPedido() != EstadoPedido.PROCESANDO) {
            throw new InvalidOrderStateException(
                    "No se puede completar pedido en estado " + pedido.getEstadoPedido()
            );
        }

        pedido.marcarComoCompletado();
        if (puntosGanados != null) {
            pedido.setTotalPuntosGanados(puntosGanados);
        }

        return pedidoRepository.save(pedido);
    }

    @Override
    public Pedido cancelOrder(UUID orderId) {
        logger.info("Cancelando pedido {}", orderId);

        Pedido pedido = getOrderById(orderId);

        if (pedido.getEstadoPedido() == EstadoPedido.COMPLETADO) {
            throw new InvalidOrderStateException(
                    "No se puede cancelar un pedido completado"
            );
        }

        pedido.setEstadoPedido(EstadoPedido.CANCELADO);

        return pedidoRepository.save(pedido);
    }

    @Override
    public String generateOrderNumber() {
        // Formato: ORD-YYYYMMDD-XXXXX
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = orderCounter.getAndIncrement();
        String counterPart = String.format("%05d", counter % 100000);

        String orderNumber = "ORD-" + datePart + "-" + counterPart;
        logger.debug("Número de pedido generado: {}", orderNumber);

        return orderNumber;
    }
}
