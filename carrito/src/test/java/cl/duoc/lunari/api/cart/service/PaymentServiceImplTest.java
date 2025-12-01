package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.TransbankConfirmResponse;
import cl.duoc.lunari.api.cart.dto.TransbankInitResponse;
import cl.duoc.lunari.api.cart.exception.PaymentNotFoundException;
import cl.duoc.lunari.api.cart.model.*;
import cl.duoc.lunari.api.cart.repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private TransbankService transbankService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID pedidoId;
    private UUID pagoId;
    private Pedido pedido;
    private Pago pago;

    @BeforeEach
    void setUp() {
        pedidoId = UUID.randomUUID();
        pagoId = UUID.randomUUID();

        pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setNumeroPedido("ORD-20250130-00001");
        pedido.setTotalProductos(new BigDecimal("50000"));

        pago = new Pago();
        pago.setId(pagoId);
        pago.setPedido(pedido);
        pago.setMetodoPago(MetodoPago.WEBPAY_PLUS);
        pago.setEstadoPago(EstadoPago.PENDIENTE);
        pago.setMontoTotal(new BigDecimal("50000"));
        pago.setTransbankToken("test-token-123");
    }

    @Test
    @DisplayName("Should initiate payment successfully")
    void initiatePayment_Success() {
        // Given
        String returnUrl = "http://localhost:3000/checkout/return";
        TransbankInitResponse transbankResponse = new TransbankInitResponse(
                "test-token-123",
                "https://webpay3gint.transbank.cl/webpayserver/initTransaction"
        );

        when(transbankService.createTransaction(any(), anyString(), anyString(), anyString()))
                .thenReturn(transbankResponse);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(orderService.updateOrderStatus(any(), any())).thenReturn(pedido);

        // When
        TransbankInitResponse result = paymentService.initiatePayment(pedido, returnUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("test-token-123");
        assertThat(result.getUrl()).contains("webpay3gint.transbank.cl");

        verify(transbankService).createTransaction(
                eq(new BigDecimal("50000")),
                eq("BUY-ORD-20250130-00001"),
                eq("SES-" + pedidoId.toString()),
                eq(returnUrl)
        );
        verify(pagoRepository).save(any(Pago.class));
        verify(orderService).updateOrderStatus(pedidoId, EstadoPedido.PAGO_PENDIENTE);
    }

    @Test
    @DisplayName("Should confirm approved payment successfully")
    void confirmPayment_Approved() {
        // Given
        String token = "test-token-123";
        TransbankConfirmResponse confirmResponse = new TransbankConfirmResponse();
        confirmResponse.setBuyOrder("BUY-ORD-20250130-00001");
        confirmResponse.setAuthorizationCode("123456");
        confirmResponse.setResponseCode(0);
        confirmResponse.setAmount(new BigDecimal("50000"));

        when(pagoRepository.findByTransbankToken(token)).thenReturn(Optional.of(pago));
        when(transbankService.confirmTransaction(token)).thenReturn(confirmResponse);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(orderService.updateOrderStatus(any(), any())).thenReturn(pedido);

        // When
        Pago result = paymentService.confirmPayment(token);

        // Then
        assertThat(result).isNotNull();
        verify(pagoRepository).findByTransbankToken(token);
        verify(transbankService).confirmTransaction(token);
        verify(paymentService).processApprovedPayment(pago, confirmResponse);
        verify(orderService).updateOrderStatus(pedidoId, EstadoPedido.PAGO_COMPLETADO);
    }

    @Test
    @DisplayName("Should confirm rejected payment")
    void confirmPayment_Rejected() {
        // Given
        String token = "test-token-123";
        TransbankConfirmResponse confirmResponse = new TransbankConfirmResponse();
        confirmResponse.setBuyOrder("BUY-ORD-20250130-00001");
        confirmResponse.setResponseCode(-1);
        confirmResponse.setAmount(new BigDecimal("50000"));

        when(pagoRepository.findByTransbankToken(token)).thenReturn(Optional.of(pago));
        when(transbankService.confirmTransaction(token)).thenReturn(confirmResponse);

        // When/Then
        assertThatThrownBy(() -> paymentService.confirmPayment(token))
                .isInstanceOf(Exception.class);

        verify(pagoRepository).findByTransbankToken(token);
        verify(transbankService).confirmTransaction(token);
    }

    @Test
    @DisplayName("Should throw exception when payment not found by token")
    void confirmPayment_NotFound() {
        // Given
        String token = "invalid-token";
        when(pagoRepository.findByTransbankToken(token)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.confirmPayment(token))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(pagoRepository).findByTransbankToken(token);
        verifyNoInteractions(transbankService);
    }

    @Test
    @DisplayName("Should get payment by ID successfully")
    void getPaymentById_Success() {
        // Given
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        // When
        Pago result = paymentService.getPaymentById(pagoId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(pagoId);

        verify(pagoRepository).findById(pagoId);
    }

    @Test
    @DisplayName("Should throw exception when payment not found by ID")
    void getPaymentById_NotFound() {
        // Given
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.getPaymentById(pagoId))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(pagoRepository).findById(pagoId);
    }

    @Test
    @DisplayName("Should get payment by order ID successfully")
    void getPaymentByOrderId_Success() {
        // Given
        when(pagoRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(pago));

        // When
        Pago result = paymentService.getPaymentByOrderId(pedidoId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPedido().getId()).isEqualTo(pedidoId);

        verify(pagoRepository).findByPedidoId(pedidoId);
    }

    @Test
    @DisplayName("Should get payment by token successfully")
    void getPaymentByToken_Success() {
        // Given
        String token = "test-token-123";
        when(pagoRepository.findByTransbankToken(token)).thenReturn(Optional.of(pago));

        // When
        Pago result = paymentService.getPaymentByToken(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransbankToken()).isEqualTo(token);

        verify(pagoRepository).findByTransbankToken(token);
    }

    @Test
    @DisplayName("Should process approved payment correctly")
    void processApprovedPayment_Success() {
        // Given
        TransbankConfirmResponse confirmResponse = new TransbankConfirmResponse();
        confirmResponse.setAuthorizationCode("123456");
        confirmResponse.setResponseCode(0);

        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(orderService.updateOrderStatus(any(), any())).thenReturn(pedido);

        // When
        paymentService.processApprovedPayment(pago, confirmResponse);

        // Then
        verify(pagoRepository).save(pago);
        verify(orderService).updateOrderStatus(pedidoId, EstadoPedido.PAGO_COMPLETADO);
    }

    @Test
    @DisplayName("Should process rejected payment correctly")
    void processRejectedPayment_ThrowsException() {
        // Given
        TransbankConfirmResponse confirmResponse = new TransbankConfirmResponse();
        confirmResponse.setResponseCode(-1);

        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(orderService.updateOrderStatus(any(), any())).thenReturn(pedido);

        // When/Then
        assertThatThrownBy(() -> paymentService.processRejectedPayment(pago, confirmResponse))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Pago rechazado");

        verify(pagoRepository).save(pago);
        verify(orderService).updateOrderStatus(pedidoId, EstadoPedido.FALLIDO);
    }
}
