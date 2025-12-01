package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.exception.EmptyCartException;
import cl.duoc.lunari.api.cart.exception.InvalidOrderStateException;
import cl.duoc.lunari.api.cart.exception.OrderNotFoundException;
import cl.duoc.lunari.api.cart.model.*;
import cl.duoc.lunari.api.cart.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID carritoId;
    private UUID usuarioId;
    private UUID pedidoId;
    private Carrito carrito;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        carritoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();

        // Setup Carrito
        carrito = new Carrito();
        carrito.setId(carritoId);
        carrito.setUsuarioId(usuarioId);

        CarritoItem item = new CarritoItem();
        item.setServicioId(1);
        item.setCantidad(2);
        item.setPrecioUnitario(new BigDecimal("25000"));
        item.setSubtotal(new BigDecimal("50000"));

        List<CarritoItem> items = new ArrayList<>();
        items.add(item);
        carrito.setItems(items);

        // Setup Pedido
        pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setNumeroPedido("ORD-20250130-00001");
        pedido.setCarritoId(carritoId);
        pedido.setUsuarioId(usuarioId);
        pedido.setEstadoPedido(EstadoPedido.CREADO);
        pedido.setTotalProductos(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should create order from cart successfully")
    void createOrderFromCart_Success() {
        // Given
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // When
        Pedido result = orderService.createOrderFromCart(carrito, "Test notes");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCarritoId()).isEqualTo(carritoId);
        assertThat(result.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(result.getEstadoPedido()).isEqualTo(EstadoPedido.CREADO);
        assertThat(result.getTotalProductos()).isEqualByComparingTo(new BigDecimal("50000"));

        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Should throw exception when creating order from empty cart")
    void createOrderFromCart_EmptyCart() {
        // Given
        carrito.setItems(new ArrayList<>());

        // When/Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(carrito, null))
                .isInstanceOf(EmptyCartException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cart items is null")
    void createOrderFromCart_NullItems() {
        // Given
        carrito.setItems(null);

        // When/Then
        assertThatThrownBy(() -> orderService.createOrderFromCart(carrito, null))
                .isInstanceOf(EmptyCartException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void getOrderById_Success() {
        // Given
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When
        Pedido result = orderService.getOrderById(pedidoId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(pedidoId);
        assertThat(result.getNumeroPedido()).isEqualTo("ORD-20250130-00001");

        verify(pedidoRepository).findById(pedidoId);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void getOrderById_NotFound() {
        // Given
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.getOrderById(pedidoId))
                .isInstanceOf(OrderNotFoundException.class);

        verify(pedidoRepository).findById(pedidoId);
    }

    @Test
    @DisplayName("Should get order by numero successfully")
    void getOrderByNumero_Success() {
        // Given
        String numeroOrden = "ORD-20250130-00001";
        when(pedidoRepository.findByNumeroPedido(numeroOrden)).thenReturn(Optional.of(pedido));

        // When
        Pedido result = orderService.getOrderByNumero(numeroOrden);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNumeroPedido()).isEqualTo(numeroOrden);

        verify(pedidoRepository).findByNumeroPedido(numeroOrden);
    }

    @Test
    @DisplayName("Should get orders by usuario")
    void getOrdersByUsuario_Success() {
        // Given
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByUsuarioIdOrderByCreadoElDesc(usuarioId)).thenReturn(pedidos);

        // When
        List<Pedido> result = orderService.getOrdersByUsuario(usuarioId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(pedido);

        verify(pedidoRepository).findByUsuarioIdOrderByCreadoElDesc(usuarioId);
    }

    @Test
    @DisplayName("Should get orders by usuario and estado")
    void getOrdersByUsuarioAndEstado_Success() {
        // Given
        EstadoPedido estado = EstadoPedido.CREADO;
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByUsuarioIdAndEstadoPedidoOrderByCreadoElDesc(usuarioId, estado))
                .thenReturn(pedidos);

        // When
        List<Pedido> result = orderService.getOrdersByUsuarioAndEstado(usuarioId, estado);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstadoPedido()).isEqualTo(EstadoPedido.CREADO);

        verify(pedidoRepository).findByUsuarioIdAndEstadoPedidoOrderByCreadoElDesc(usuarioId, estado);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void updateOrderStatus_Success() {
        // Given
        EstadoPedido nuevoEstado = EstadoPedido.PAGO_PENDIENTE;
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // When
        Pedido result = orderService.updateOrderStatus(pedidoId, nuevoEstado);

        // Then
        assertThat(result).isNotNull();
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Should mark order as complete from PAGO_COMPLETADO state")
    void markOrderComplete_FromPagoCompletado() {
        // Given
        pedido.setEstadoPedido(EstadoPedido.PAGO_COMPLETADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // When
        Pedido result = orderService.markOrderComplete(pedidoId, 500);

        // Then
        assertThat(result).isNotNull();
        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Should mark order as complete from PROCESANDO state")
    void markOrderComplete_FromProcesando() {
        // Given
        pedido.setEstadoPedido(EstadoPedido.PROCESANDO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // When
        Pedido result = orderService.markOrderComplete(pedidoId, 500);

        // Then
        assertThat(result).isNotNull();
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Should throw exception when marking order as complete from invalid state")
    void markOrderComplete_InvalidState() {
        // Given
        pedido.setEstadoPedido(EstadoPedido.CREADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When/Then
        assertThatThrownBy(() -> orderService.markOrderComplete(pedidoId, 500))
                .isInstanceOf(InvalidOrderStateException.class);

        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void cancelOrder_Success() {
        // Given
        pedido.setEstadoPedido(EstadoPedido.CREADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        // When
        Pedido result = orderService.cancelOrder(pedidoId);

        // Then
        assertThat(result).isNotNull();
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    @DisplayName("Should throw exception when canceling completed order")
    void cancelOrder_AlreadyCompleted() {
        // Given
        pedido.setEstadoPedido(EstadoPedido.COMPLETADO);
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When/Then
        assertThatThrownBy(() -> orderService.cancelOrder(pedidoId))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("No se puede cancelar un pedido completado");

        verify(pedidoRepository).findById(pedidoId);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should generate unique order numbers")
    void generateOrderNumber_Unique() {
        // When
        String orderNumber1 = orderService.generateOrderNumber();
        String orderNumber2 = orderService.generateOrderNumber();

        // Then
        assertThat(orderNumber1).isNotNull();
        assertThat(orderNumber2).isNotNull();
        assertThat(orderNumber1).startsWith("ORD-");
        assertThat(orderNumber2).startsWith("ORD-");
        assertThat(orderNumber1).isNotEqualTo(orderNumber2);
    }

    @Test
    @DisplayName("Should calculate points correctly when creating order")
    void createOrderFromCart_CalculatesPoints() {
        // Given
        carrito.getItems().get(0).setSubtotal(new BigDecimal("100000"));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Pedido result = orderService.createOrderFromCart(carrito, null);

        // Then
        assertThat(result.getTotalPuntosGanados()).isEqualTo(1000); // 100000 / 100 = 1000 points
    }
}
