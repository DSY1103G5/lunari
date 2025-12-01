package cl.duoc.lunari.api.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para solicitar reducción de stock en el servicio de inventario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReductionRequest {

    @NotEmpty(message = "La lista de items no puede estar vacía")
    private List<StockItem> items;

    /**
     * Representa un item individual para reducción de stock
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem {
        @NotNull(message = "ID del producto no puede estar vacío")
        private Long productoId;

        @NotNull(message = "Cantidad no puede estar vacía")
        @Min(value = 1, message = "Cantidad debe ser mayor a 0")
        private Integer cantidad;
    }
}
