package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    Optional<Producto> findByCode(String code);

    List<Producto> findByCategoriaIdCategoria(String categoriaId);

    List<Producto> findByIsActivoTrue();

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByMarca(String marca);

    List<Producto> findByStockGreaterThan(Integer stock);

    List<Producto> findByPrecioCLPBetween(Integer min, Integer max);

    List<Producto> findByRatingGreaterThanEqual(BigDecimal rating);

    // JSONB queries for tags
    @Query(value = "SELECT * FROM producto WHERE tags @> CAST(:tag AS jsonb)", nativeQuery = true)
    List<Producto> findByTag(@Param("tag") String tag);
}
