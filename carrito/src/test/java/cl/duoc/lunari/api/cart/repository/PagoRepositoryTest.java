package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for PagoRepository using H2 in-memory database
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@DisplayName("PagoRepository Tests")
class PagoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // Create a test pedido
        pedido = new Pedido();
        pedido.setNumeroPedido("ORD-20250130-00001");
        pedido.setCarritoId(UUID.randomUUID());
        pedido.setUsuarioId(UUID.randomUUID());
        pedido.setEstadoPedido(EstadoPedido.PAGO_PENDIENTE);
        pedido.setTotalProductos(new BigDecimal("50000"));
        pedido.setTotalPuntosGanados(500);

        pedido = pedidoRepository.save(pedido);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and find pago by ID")
    void savePago_FindById() {
        // Given
        Pago pago = createTestPago("token-123", EstadoPago.PENDIENTE);

        // When
        Pago saved = pagoRepository.save(pago);
        entityManager.flush();

        Optional<Pago> found = pagoRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTransbankToken()).isEqualTo("token-123");
        assertThat(found.get().getEstadoPago()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(found.get().getMontoTotal()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should find pago by transbank token")
    void findByTransbankToken() {
        // Given
        Pago pago = createTestPago("token-unique-123", EstadoPago.PENDIENTE);
        pagoRepository.save(pago);
        entityManager.flush();

        // When
        Optional<Pago> found = pagoRepository.findByTransbankToken("token-unique-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTransbankToken()).isEqualTo("token-unique-123");
    }

    @Test
    @DisplayName("Should return empty when token not found")
    void findByTransbankToken_NotFound() {
        // When
        Optional<Pago> found = pagoRepository.findByTransbankToken("non-existent-token");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find pago by pedido ID")
    void findByPedidoId() {
        // Given
        Pago pago = createTestPago("token-123", EstadoPago.APROBADO);
        pagoRepository.save(pago);
        entityManager.flush();

        // When
        Optional<Pago> found = pagoRepository.findByPedidoId(pedido.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPedido().getId()).isEqualTo(pedido.getId());
    }

    @Test
    @DisplayName("Should find pagos by estado")
    void findByEstadoPago() {
        // Given
        pagoRepository.save(createTestPago("token-1", EstadoPago.APROBADO));
        pagoRepository.save(createTestPago("token-2", EstadoPago.PENDIENTE));

        // Create another pedido for second payment
        Pedido pedido2 = new Pedido();
        pedido2.setNumeroPedido("ORD-20250130-00002");
        pedido2.setCarritoId(UUID.randomUUID());
        pedido2.setUsuarioId(UUID.randomUUID());
        pedido2.setEstadoPedido(EstadoPedido.PAGO_PENDIENTE);
        pedido2.setTotalProductos(new BigDecimal("30000"));
        pedido2 = pedidoRepository.save(pedido2);

        Pago pago3 = new Pago();
        pago3.setPedido(pedido2);
        pago3.setMetodoPago(MetodoPago.WEBPAY_PLUS);
        pago3.setEstadoPago(EstadoPago.APROBADO);
        pago3.setMontoTotal(new BigDecimal("30000"));
        pago3.setTransbankToken("token-3");
        pagoRepository.save(pago3);

        entityManager.flush();

        // When
        List<Pago> pagosAprobados = pagoRepository.findByEstadoPagoOrderByCreadoElDesc(EstadoPago.APROBADO);

        // Then
        assertThat(pagosAprobados).hasSize(2);
        assertThat(pagosAprobados).extracting(Pago::getEstadoPago)
                .containsOnly(EstadoPago.APROBADO);
    }

    @Test
    @DisplayName("Should update pago estado")
    void updatePagoEstado() {
        // Given
        Pago pago = createTestPago("token-123", EstadoPago.PENDIENTE);
        Pago saved = pagoRepository.save(pago);
        entityManager.flush();

        // When
        saved.setEstadoPago(EstadoPago.APROBADO);
        saved.setAuthorizationCode("AUTH-123456");
        pagoRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Pago> updated = pagoRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEstadoPago()).isEqualTo(EstadoPago.APROBADO);
        assertThat(updated.get().getAuthorizationCode()).isEqualTo("AUTH-123456");
    }

    @Test
    @DisplayName("Should mark pago as approved with authorization code")
    void markPagoAsApproved() {
        // Given
        Pago pago = createTestPago("token-123", EstadoPago.PENDIENTE);
        Pago saved = pagoRepository.save(pago);
        entityManager.flush();

        // When
        saved.marcarComoAprobado("AUTH-123456", 0);
        pagoRepository.save(saved);
        entityManager.flush();

        // Then
        Optional<Pago> updated = pagoRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().estaAprobado()).isTrue();
        assertThat(updated.get().getAuthorizationCode()).isEqualTo("AUTH-123456");
    }

    @Test
    @DisplayName("Should mark pago as rejected")
    void markPagoAsRejected() {
        // Given
        Pago pago = createTestPago("token-123", EstadoPago.PENDIENTE);
        Pago saved = pagoRepository.save(pago);
        entityManager.flush();

        // When
        saved.marcarComoRechazado(-1);
        pagoRepository.save(saved);
        entityManager.flush();

        // Then
        Optional<Pago> updated = pagoRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEstadoPago()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(updated.get().estaAprobado()).isFalse();
    }

    @Test
    @DisplayName("Should maintain relationship with pedido")
    void maintainPedidoRelationship() {
        // Given
        Pago pago = createTestPago("token-123", EstadoPago.APROBADO);
        Pago saved = pagoRepository.save(pago);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Pago> found = pagoRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPedido()).isNotNull();
        assertThat(found.get().getPedido().getNumeroPedido()).isEqualTo("ORD-20250130-00001");
    }

    @Test
    @DisplayName("Should count pagos by estado")
    void countByEstadoPago() {
        // Given
        pagoRepository.save(createTestPago("token-1", EstadoPago.APROBADO));

        // Create another pedido
        Pedido pedido2 = new Pedido();
        pedido2.setNumeroPedido("ORD-20250130-00002");
        pedido2.setCarritoId(UUID.randomUUID());
        pedido2.setUsuarioId(UUID.randomUUID());
        pedido2.setEstadoPedido(EstadoPedido.PAGO_PENDIENTE);
        pedido2.setTotalProductos(new BigDecimal("30000"));
        pedido2 = pedidoRepository.save(pedido2);

        Pago pago2 = new Pago();
        pago2.setPedido(pedido2);
        pago2.setMetodoPago(MetodoPago.WEBPAY_PLUS);
        pago2.setEstadoPago(EstadoPago.PENDIENTE);
        pago2.setMontoTotal(new BigDecimal("30000"));
        pago2.setTransbankToken("token-2");
        pagoRepository.save(pago2);

        entityManager.flush();

        // When
        long countAprobados = pagoRepository.countByEstadoPago(EstadoPago.APROBADO);
        long countPendientes = pagoRepository.countByEstadoPago(EstadoPago.PENDIENTE);

        // Then
        assertThat(countAprobados).isEqualTo(1);
        assertThat(countPendientes).isEqualTo(1);
    }

    private Pago createTestPago(String token, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(MetodoPago.WEBPAY_PLUS);
        pago.setEstadoPago(estado);
        pago.setMontoTotal(new BigDecimal("50000"));
        pago.setTransbankToken(token);
        pago.setTransbankBuyOrder("BUY-" + pedido.getNumeroPedido());
        pago.setTransbankSessionId("SES-" + pedido.getId().toString());
        pago.setPaymentUrl("https://webpay3gint.transbank.cl/webpayserver/initTransaction");
        return pago;
    }
}
