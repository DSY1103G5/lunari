package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.EstadoPago;
import cl.duoc.lunari.api.cart.model.MetodoPago;
import cl.duoc.lunari.api.cart.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Pago
 * Proporciona métodos de acceso a datos para pagos
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {

    /**
     * Encuentra un pago por ID de pedido
     */
    Optional<Pago> findByPedidoId(UUID pedidoId);

    /**
     * Encuentra un pago por token de Transbank
     */
    Optional<Pago> findByTransbankToken(String transbankToken);

    /**
     * Encuentra un pago por buy order de Transbank
     */
    Optional<Pago> findByTransbankBuyOrder(String transbankBuyOrder);

    /**
     * Encuentra pagos por estado
     */
    List<Pago> findByEstadoPagoOrderByCreadoElDesc(EstadoPago estadoPago);

    /**
     * Encuentra pagos por método de pago
     */
    List<Pago> findByMetodoPagoOrderByCreadoElDesc(MetodoPago metodoPago);

    /**
     * Cuenta pagos por estado
     */
    Long countByEstadoPago(EstadoPago estadoPago);

    /**
     * Encuentra pagos pendientes más antiguos que una fecha específica
     * Útil para identificar pagos expirados que nunca recibieron callback
     */
    @Query("SELECT p FROM Pago p WHERE p.estadoPago = :estado AND p.creadoEl < :fechaLimite")
    List<Pago> findPagosPendientesAntiguos(
            @Param("estado") EstadoPago estado,
            @Param("fechaLimite") OffsetDateTime fechaLimite
    );

    /**
     * Encuentra pagos confirmados en un rango de fechas
     */
    List<Pago> findByEstadoPagoAndConfirmadoElBetweenOrderByConfirmadoElDesc(
            EstadoPago estadoPago,
            OffsetDateTime fechaInicio,
            OffsetDateTime fechaFin
    );

    /**
     * Verifica si existe un pago para un pedido específico
     */
    boolean existsByPedidoId(UUID pedidoId);

    /**
     * Encuentra pagos por código de respuesta de Transbank
     * Útil para análisis de rechazos
     */
    List<Pago> findByResponseCode(Integer responseCode);
}
