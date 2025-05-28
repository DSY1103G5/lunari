package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.CarritoServicioAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarritoServicioAdicionalRepository extends JpaRepository<CarritoServicioAdicional, UUID> {
    
    List<CarritoServicioAdicional> findByCarritoItemId(UUID carritoItemId);
    
    Optional<CarritoServicioAdicional> findByCarritoItemIdAndServicioAdicionalId(
        UUID carritoItemId, 
        Integer servicioAdicionalId
    );
    
    List<CarritoServicioAdicional> findByServicioAdicionalId(Integer servicioAdicionalId);
    
    long countByCarritoItemId(UUID carritoItemId);
    
    @Query("SELECT SUM(csa.precioAdicional) FROM CarritoServicioAdicional csa WHERE csa.carritoItem.id = :carritoItemId")
    BigDecimal calculateTotalServiciosAdicionalesByItemId(@Param("carritoItemId") UUID carritoItemId);
    
    boolean existsByCarritoItemIdAndServicioAdicionalId(
        UUID carritoItemId, 
        Integer servicioAdicionalId
    );
    
    void deleteByCarritoItemId(UUID carritoItemId);
    
    @Query("SELECT csa FROM CarritoServicioAdicional csa WHERE csa.carritoItem.carrito.id = :carritoId")
    List<CarritoServicioAdicional> findByCarritoId(@Param("carritoId") UUID carritoId);
    
    @Query("SELECT SUM(csa.precioAdicional) FROM CarritoServicioAdicional csa WHERE csa.carritoItem.carrito.id = :carritoId")
    BigDecimal calculateTotalServiciosAdicionalesByCarritoId(@Param("carritoId") UUID carritoId);
}