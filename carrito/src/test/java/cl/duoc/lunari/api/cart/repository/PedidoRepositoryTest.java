package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.EstadoPedido;
import cl.duoc.lunari.api.cart.model.Pedido;
import cl.duoc.lunari.api.cart.model.PedidoItem;
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
 * Repository tests for PedidoRepository using H2 in-memory database
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@DisplayName("PedidoRepository Tests")
class PedidoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PedidoRepository pedidoRepository;

    private UUID usuarioId;
    private UUID carritoId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        carritoId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and find pedido by ID")
    void savePedido_FindById() {
        // Given
        Pedido pedido = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);

        // When
        Pedido saved = pedidoRepository.save(pedido);
        entityManager.flush();

        Optional<Pedido> found = pedidoRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNumeroPedido()).isEqualTo("ORD-20250130-00001");
        assertThat(found.get().getEstadoPedido()).isEqualTo(EstadoPedido.CREADO);
        assertThat(found.get().getTotalProductos()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Should find pedido by numero")
    void findByNumeroPedido() {
        // Given
        Pedido pedido = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);
        pedidoRepository.save(pedido);
        entityManager.flush();

        // When
        Optional<Pedido> found = pedidoRepository.findByNumeroPedido("ORD-20250130-00001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNumeroPedido()).isEqualTo("ORD-20250130-00001");
    }

    @Test
    @DisplayName("Should find pedidos by usuario ID ordered by creation date")
    void findByUsuarioId_OrderedByDate() {
        // Given
        Pedido pedido1 = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);
        Pedido pedido2 = createTestPedido("ORD-20250130-00002", EstadoPedido.COMPLETADO);

        pedidoRepository.save(pedido1);
        entityManager.flush();

        // Add delay to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException e) { }

        pedidoRepository.save(pedido2);
        entityManager.flush();

        // When
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByCreadoElDesc(usuarioId);

        // Then
        assertThat(pedidos).hasSize(2);
        // Most recent first
        assertThat(pedidos.get(0).getNumeroPedido()).isEqualTo("ORD-20250130-00002");
        assertThat(pedidos.get(1).getNumeroPedido()).isEqualTo("ORD-20250130-00001");
    }

    @Test
    @DisplayName("Should find pedidos by usuario and estado")
    void findByUsuarioIdAndEstado() {
        // Given
        Pedido pedido1 = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);
        Pedido pedido2 = createTestPedido("ORD-20250130-00002", EstadoPedido.COMPLETADO);
        Pedido pedido3 = createTestPedido("ORD-20250130-00003", EstadoPedido.CREADO);

        pedidoRepository.save(pedido1);
        pedidoRepository.save(pedido2);
        pedidoRepository.save(pedido3);
        entityManager.flush();

        // When
        List<Pedido> pedidosCreados = pedidoRepository
                .findByUsuarioIdAndEstadoPedidoOrderByCreadoElDesc(usuarioId, EstadoPedido.CREADO);

        // Then
        assertThat(pedidosCreados).hasSize(2);
        assertThat(pedidosCreados).extracting(Pedido::getEstadoPedido)
                .containsOnly(EstadoPedido.CREADO);
    }

    @Test
    @DisplayName("Should count pedidos by estado")
    void countByEstadoPedido() {
        // Given
        Pedido pedido1 = createTestPedido("ORD-20250130-00001", EstadoPedido.PAGO_PENDIENTE);
        Pedido pedido2 = createTestPedido("ORD-20250130-00002", EstadoPedido.PAGO_PENDIENTE);

        pedidoRepository.save(pedido1);
        pedidoRepository.save(pedido2);
        entityManager.flush();

        // When
        long count = pedidoRepository.countByEstadoPedido(EstadoPedido.PAGO_PENDIENTE);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find pedidos with items using fetch join")
    void findPedidoWithItems() {
        // Given
        Pedido pedido = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);

        PedidoItem item1 = new PedidoItem();
        item1.setProductoId(1L);
        item1.setCodigoProducto("PROD-001");
        item1.setNombreProducto("Producto 1");
        item1.setCantidad(2);
        item1.setPrecioUnitario(new BigDecimal("25000"));
        item1.setSubtotal(new BigDecimal("50000"));

        pedido.agregarItem(item1);

        Pedido saved = pedidoRepository.save(pedido);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context

        // When
        Optional<Pedido> found = pedidoRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getItems()).isNotEmpty();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getCodigoProducto()).isEqualTo("PROD-001");
    }

    @Test
    @DisplayName("Should count all pedidos by estado Completado")
    void countAllPedidosByEstadoCompletado() {
        // Given
        pedidoRepository.save(createTestPedido("ORD-1", EstadoPedido.COMPLETADO));
        pedidoRepository.save(createTestPedido("ORD-2", EstadoPedido.COMPLETADO));
        pedidoRepository.save(createTestPedido("ORD-3", EstadoPedido.CANCELADO));
        entityManager.flush();

        // When
        long count = pedidoRepository.countByEstadoPedido(EstadoPedido.COMPLETADO);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should update pedido estado")
    void updatePedidoEstado() {
        // Given
        Pedido pedido = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);
        Pedido saved = pedidoRepository.save(pedido);
        entityManager.flush();

        // When
        saved.setEstadoPedido(EstadoPedido.PAGO_COMPLETADO);
        pedidoRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Pedido> updated = pedidoRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getEstadoPedido()).isEqualTo(EstadoPedido.PAGO_COMPLETADO);
    }

    @Test
    @DisplayName("Should delete pedido")
    void deletePedido() {
        // Given
        Pedido pedido = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);
        Pedido saved = pedidoRepository.save(pedido);
        entityManager.flush();

        // When
        pedidoRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<Pedido> found = pedidoRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should cascade delete pedido items")
    void cascadeDeleteItems() {
        // Given
        Pedido pedido = createTestPedido("ORD-20250130-00001", EstadoPedido.CREADO);

        PedidoItem item = new PedidoItem();
        item.setProductoId(1L);
        item.setCodigoProducto("PROD-001");
        item.setNombreProducto("Producto 1");
        item.setCantidad(1);
        item.setPrecioUnitario(new BigDecimal("50000"));
        item.setSubtotal(new BigDecimal("50000"));

        pedido.agregarItem(item);
        Pedido saved = pedidoRepository.save(pedido);
        UUID pedidoId = saved.getId();
        entityManager.flush();

        // When
        pedidoRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<Pedido> found = pedidoRepository.findById(pedidoId);
        assertThat(found).isEmpty();
        // Items should also be deleted due to cascade
    }

    private Pedido createTestPedido(String numeroPedido, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(numeroPedido);
        pedido.setCarritoId(carritoId);
        pedido.setUsuarioId(usuarioId);
        pedido.setEstadoPedido(estado);
        pedido.setTotalProductos(new BigDecimal("50000"));
        pedido.setTotalPuntosGanados(500);
        return pedido;
    }
}
