package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, UUID> {
    
    List<CarritoItem> findByCarritoId(UUID carritoId);

    Optional<CarritoItem> findByCarritoIdAndServicioId(UUID carritoId, Integer servicioId);
    
    List<CarritoItem> findByServicioId(Integer servicioId);
    
    long countByCarritoId(UUID carritoId);
    
    @Query("SELECT SUM(ci.subtotal) FROM CarritoItem ci WHERE ci.carrito.id = :carritoId")
    BigDecimal calculateSubtotalByCarritoId(@Param("carritoId") UUID carritoId);
    
    boolean existsByCarritoIdAndServicioId(UUID carritoId, Integer servicioId);
    
    void deleteByCarritoId(UUID carritoId);
    
    List<CarritoItem> findByCarritoIdAndCantidadGreaterThan(UUID carritoId, Integer cantidad);
    
    List<CarritoItem> findByCarritoIdAndPrecioUnitarioBetween(
        UUID carritoId, 
        BigDecimal precioMin, 
        BigDecimal precioMax
    );
}