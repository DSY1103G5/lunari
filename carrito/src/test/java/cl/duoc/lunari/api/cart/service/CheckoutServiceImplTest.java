package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.CheckoutInitiateRequest;
import cl.duoc.lunari.api.cart.dto.CheckoutInitiateResponse;
import cl.duoc.lunari.api.cart.dto.TransbankInitResponse;
import cl.duoc.lunari.api.cart.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CheckoutServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService Unit Tests")
class CheckoutServiceImplTest {

    @Mock
    private CarritoService carritoService;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private StockReductionJob stockReductionJob;

    @Mock
    private PointsAwardJob pointsAwardJob;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private UUID carritoId;
    private UUID usuarioId;
    private UUID pedidoId;
    private Carrito carrito;
    private Pedido pedido;
    private Pago pago;

    @BeforeEach
    void setUp() {
        carritoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();

        // Setup Carrito
        carrito = new Carrito();
        carrito.setId(carritoId);
        carrito.setUsuarioId(usuarioId);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setTotalEstimado(new BigDecimal("50000"));

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
        pedido.setTotalPuntosGanados(500);

        // Setup Pago
        pago = new Pago();
        pago.setPedido(pedido);
        pago.setEstadoPago(EstadoPago.APROBADO);
        pago.setTransbankToken("test-token-123");
    }

    @Test
    @DisplayName("Should initiate checkout successfully")
    void initiateCheckout_Success() {
        // Given
        CheckoutInitiateRequest request = new CheckoutInitiateRequest();
        request.setCarritoId(carritoId);
        request.setReturnUrl("http://localhost:3000/checkout/return");
        request.setNotasCliente("Test order");

        TransbankInitResponse transbankResponse = new TransbankInitResponse(
                "test-token-123",
                "https://webpay3gint.transbank.cl/webpayserver/initTransaction"
        );

        when(carritoService.obtenerCarritoPorId(carritoId)).thenReturn(carrito);
        when(orderService.createOrderFromCart(eq(carrito), anyString())).thenReturn(pedido);
        when(paymentService.initiatePayment(eq(pedido), anyString())).thenReturn(transbankResponse);
        when(carritoService.markCartProcessed(any(), anyString())).thenReturn(carrito);

        // When
        CheckoutInitiateResponse response = checkoutService.initiateCheckout(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(pedidoId);
        assertThat(response.getNumeroOrden()).isEqualTo("ORD-20250130-00001");
        assertThat(response.getPaymentUrl()).isEqualTo("https://webpay3gint.transbank.cl/webpayserver/initTransaction");
        assertThat(response.getTransbankToken()).isEqualTo("test-token-123");

        verify(carritoService).obtenerCarritoPorId(carritoId);
        verify(orderService).createOrderFromCart(carrito, "Test order");
        verify(paymentService).initiatePayment(pedido, "http://localhost:3000/checkout/return");
        verify(carritoService).markCartProcessed(carritoId, "ORD-20250130-00001");
    }

    @Test
    @DisplayName("Should throw exception when cart not found")
    void initiateCheckout_CartNotFound() {
        // Given
        CheckoutInitiateRequest request = new CheckoutInitiateRequest();
        request.setCarritoId(carritoId);
        request.setReturnUrl("http://localhost:3000/checkout/return");

        when(carritoService.obtenerCarritoPorId(carritoId))
                .thenThrow(new RuntimeException("Carrito no encontrado"));

        // When/Then
        assertThatThrownBy(() -> checkoutService.initiateCheckout(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Carrito no encontrado");

        verify(carritoService).obtenerCarritoPorId(carritoId);
        verifyNoInteractions(orderService, paymentService);
    }

    @Test
    @DisplayName("Should confirm checkout with approved payment")
    void confirmCheckout_ApprovedPayment() {
        // Given
        String token = "test-token-123";
        pago.setEstadoPago(EstadoPago.APROBADO);

        when(paymentService.confirmPayment(token)).thenReturn(pago);

        // When
        Pedido result = checkoutService.confirmCheckout(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(pedido);

        verify(paymentService).confirmPayment(token);
    }

    @Test
    @DisplayName("Should confirm checkout with rejected payment")
    void confirmCheckout_RejectedPayment() {
        // Given
        String token = "test-token-123";
        pago.setEstadoPago(EstadoPago.RECHAZADO);

        when(paymentService.confirmPayment(token)).thenReturn(pago);

        // When
        Pedido result = checkoutService.confirmCheckout(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(pedido);

        verify(paymentService).confirmPayment(token);
        // processPostPaymentActions no debería llamarse para pagos rechazados
    }

    @Test
    @DisplayName("Should process post-payment actions for approved payment")
    void processPostPaymentActions_Success() {
        // Given
        pedido.setEstadoPedido(EstadoPedido.PAGO_COMPLETADO);

        doNothing().when(stockReductionJob).execute(any(Pedido.class));
        doNothing().when(pointsAwardJob).execute(any(Pedido.class));
        when(orderService.markOrderComplete(any(), anyInt())).thenReturn(pedido);

        // When
        checkoutService.processPostPaymentActions(pedido);

        // Then
        // Verificar que se llamaron los jobs (puede tomar tiempo por @Async, pero en test unitario es síncrono)
        verify(stockReductionJob, timeout(1000)).execute(pedido);
        verify(pointsAwardJob, timeout(1000)).execute(pedido);
        verify(orderService, timeout(1000)).markOrderComplete(pedidoId, 500);
    }

    @Test
    @DisplayName("Should handle errors gracefully in post-payment actions")
    void processPostPaymentActions_WithError() {
        // Given
        doThrow(new RuntimeException("Stock service error"))
                .when(stockReductionJob).execute(any(Pedido.class));

        // When/Then - No debería lanzar excepción, solo logear el error
        assertThatCode(() -> checkoutService.processPostPaymentActions(pedido))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should use default notes when not provided")
    void initiateCheckout_WithoutNotes() {
        // Given
        CheckoutInitiateRequest request = new CheckoutInitiateRequest();
        request.setCarritoId(carritoId);
        request.setReturnUrl("http://localhost:3000/checkout/return");
        request.setNotasCliente(null);

        TransbankInitResponse transbankResponse = new TransbankInitResponse(
                "test-token-123",
                "https://webpay3gint.transbank.cl/webpayserver/initTransaction"
        );

        when(carritoService.obtenerCarritoPorId(carritoId)).thenReturn(carrito);
        when(orderService.createOrderFromCart(eq(carrito), isNull())).thenReturn(pedido);
        when(paymentService.initiatePayment(eq(pedido), anyString())).thenReturn(transbankResponse);
        when(carritoService.markCartProcessed(any(), anyString())).thenReturn(carrito);

        // When
        CheckoutInitiateResponse response = checkoutService.initiateCheckout(request);

        // Then
        assertThat(response).isNotNull();
        verify(orderService).createOrderFromCart(carrito, null);
    }
}
