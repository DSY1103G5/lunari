package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.EstadoPedido;
import cl.duoc.lunari.api.cart.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Pedido
 * Proporciona métodos de acceso a datos para pedidos
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    /**
     * Encuentra un pedido por su número de pedido
     */
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    /**
     * Encuentra todos los pedidos de un usuario
     */
    List<Pedido> findByUsuarioIdOrderByCreadoElDesc(UUID usuarioId);

    /**
     * Encuentra pedidos de un usuario por estado
     */
    List<Pedido> findByUsuarioIdAndEstadoPedidoOrderByCreadoElDesc(UUID usuarioId, EstadoPedido estadoPedido);

    /**
     * Encuentra pedidos por estado
     */
    List<Pedido> findByEstadoPedidoOrderByCreadoElDesc(EstadoPedido estadoPedido);

    /**
     * Encuentra pedidos por carrito ID
     */
    Optional<Pedido> findByCarritoId(UUID carritoId);

    /**
     * Encuentra pedidos creados dentro de un rango de fechas
     */
    List<Pedido> findByCreadoElBetweenOrderByCreadoElDesc(OffsetDateTime fechaInicio, OffsetDateTime fechaFin);

    /**
     * Cuenta pedidos por estado
     */
    Long countByEstadoPedido(EstadoPedido estadoPedido);

    /**
     * Cuenta pedidos de un usuario
     */
    Long countByUsuarioId(UUID usuarioId);

    /**
     * Encuentra pedidos pendientes de pago (estado PAGO_PENDIENTE) más antiguos que una fecha
     * Útil para identificar pagos expirados
     */
    @Query("SELECT p FROM Pedido p WHERE p.estadoPedido = :estado AND p.creadoEl < :fechaLimite")
    List<Pedido> findPedidosPendientesAntiguos(
            @Param("estado") EstadoPedido estado,
            @Param("fechaLimite") OffsetDateTime fechaLimite
    );

    /**
     * Verifica si existe un pedido para un carrito específico
     */
    boolean existsByCarritoId(UUID carritoId);
}
