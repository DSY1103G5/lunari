package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.Carrito;
import cl.duoc.lunari.api.cart.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, UUID> {
    
    Optional<Carrito> findByUsuarioIdAndEstado(UUID usuarioId, EstadoCarrito estado);
    
    List<Carrito> findByUsuarioId(UUID usuarioId);
    
    List<Carrito> findByEstado(EstadoCarrito estado);
    
    List<Carrito> findByFechaExpiracionBefore(OffsetDateTime fecha);
    
    @Query("SELECT c FROM Carrito c WHERE c.usuarioId = :usuarioId AND c.estado = 'ACTIVO'")
    Optional<Carrito> findCarritoActivoByUsuarioId(@Param("usuarioId") UUID usuarioId);
    
    long countByEstado(EstadoCarrito estado);
    
    List<Carrito> findByCreadoElBetween(OffsetDateTime fechaInicio, OffsetDateTime fechaFin);
    
    boolean existsByUsuarioIdAndEstado(UUID usuarioId, EstadoCarrito estado);
}