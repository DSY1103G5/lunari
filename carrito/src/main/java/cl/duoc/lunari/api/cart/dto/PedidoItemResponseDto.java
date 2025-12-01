package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.model.PedidoItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para PedidoItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItemResponseDto {

    private UUID id;
    private Long productoId;
    private String codigoProducto;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private OffsetDateTime creadoEl;

    /**
     * Convierte una entidad PedidoItem a DTO
     */
    public static PedidoItemResponseDto fromEntity(PedidoItem item) {
        PedidoItemResponseDto dto = new PedidoItemResponseDto();
        dto.setId(item.getId());
        dto.setProductoId(item.getProductoId());
        dto.setCodigoProducto(item.getCodigoProducto());
        dto.setNombreProducto(item.getNombreProducto());
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        dto.setCreadoEl(item.getCreadoEl());
        return dto;
    }
}
