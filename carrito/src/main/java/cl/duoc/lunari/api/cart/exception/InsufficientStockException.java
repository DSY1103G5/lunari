package cl.duoc.lunari.api.cart.exception;

/**
 * Excepción lanzada cuando no hay suficiente stock de un producto
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productoId;
    private final Integer stockDisponible;
    private final Integer cantidadSolicitada;

    public InsufficientStockException(Long productoId, Integer stockDisponible, Integer cantidadSolicitada) {
        super(String.format("Stock insuficiente para producto ID %d. Disponible: %d, Solicitado: %d",
                productoId, stockDisponible, cantidadSolicitada));
        this.productoId = productoId;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.productoId = null;
        this.stockDisponible = null;
        this.cantidadSolicitada = null;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}
