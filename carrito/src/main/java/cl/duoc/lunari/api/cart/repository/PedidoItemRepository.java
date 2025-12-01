package cl.duoc.lunari.api.cart.repository;

import cl.duoc.lunari.api.cart.model.PedidoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad PedidoItem
 * Proporciona métodos de acceso a datos para items de pedidos
 */
@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, UUID> {

    /**
     * Encuentra todos los items de un pedido
     */
    List<PedidoItem> findByPedidoId(UUID pedidoId);

    /**
     * Encuentra items por ID de producto
     * Útil para analizar qué pedidos contienen un producto específico
     */
    List<PedidoItem> findByProductoId(Long productoId);

    /**
     * Cuenta cuántos items tiene un pedido
     */
    Long countByPedidoId(UUID pedidoId);

    /**
     * Encuentra items por código de producto
     */
    List<PedidoItem> findByCodigoProducto(String codigoProducto);

    /**
     * Suma la cantidad total de un producto vendido
     */
    @Query("SELECT COALESCE(SUM(pi.cantidad), 0) FROM PedidoItem pi WHERE pi.productoId = :productoId")
    Long sumCantidadByProductoId(@Param("productoId") Long productoId);

    /**
     * Encuentra los productos más vendidos
     */
    @Query("SELECT pi.productoId, SUM(pi.cantidad) as total " +
           "FROM PedidoItem pi " +
           "GROUP BY pi.productoId " +
           "ORDER BY total DESC")
    List<Object[]> findProductosMasVendidos();
}
