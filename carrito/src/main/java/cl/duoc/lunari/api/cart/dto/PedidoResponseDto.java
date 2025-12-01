package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.model.EstadoPedido;
import cl.duoc.lunari.api.cart.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de respuesta para Pedido
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDto {

    private UUID id;
    private String numeroPedido;
    private UUID carritoId;
    private UUID usuarioId;
    private EstadoPedido estadoPedido;
    private BigDecimal totalProductos;
    private Integer totalPuntosGanados;
    private String notasCliente;
    private OffsetDateTime creadoEl;
    private OffsetDateTime actualizadoEl;
    private OffsetDateTime completadoEl;
    private List<PedidoItemResponseDto> items = new ArrayList<>();
    private PagoResponseDto pago;

    /**
     * Convierte una entidad Pedido a DTO
     */
    public static PedidoResponseDto fromEntity(Pedido pedido) {
        PedidoResponseDto dto = new PedidoResponseDto();
        dto.setId(pedido.getId());
        dto.setNumeroPedido(pedido.getNumeroPedido());
        dto.setCarritoId(pedido.getCarritoId());
        dto.setUsuarioId(pedido.getUsuarioId());
        dto.setEstadoPedido(pedido.getEstadoPedido());
        dto.setTotalProductos(pedido.getTotalProductos());
        dto.setTotalPuntosGanados(pedido.getTotalPuntosGanados());
        dto.setNotasCliente(pedido.getNotasCliente());
        dto.setCreadoEl(pedido.getCreadoEl());
        dto.setActualizadoEl(pedido.getActualizadoEl());
        dto.setCompletadoEl(pedido.getCompletadoEl());

        if (pedido.getItems() != null) {
            dto.setItems(pedido.getItems().stream()
                    .map(PedidoItemResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (pedido.getPago() != null) {
            dto.setPago(PagoResponseDto.fromEntity(pedido.getPago()));
        }

        return dto;
    }
}
