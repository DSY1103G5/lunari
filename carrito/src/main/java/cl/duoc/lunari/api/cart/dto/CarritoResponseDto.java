package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.model.Carrito;
import cl.duoc.lunari.api.cart.model.EstadoCarrito;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponseDto {
    private UUID id;
    private UUID usuarioId;
    private EstadoCarrito estado;
    private BigDecimal totalEstimado;
    private String notasCliente;
    private OffsetDateTime fechaExpiracion;
    private OffsetDateTime creadoEl;
    private OffsetDateTime actualizadoEl;
    private List<CarritoItemResponseDto> items;
    private Integer totalItems;

    public static CarritoResponseDto fromEntity(Carrito carrito) {
        CarritoResponseDto dto = new CarritoResponseDto();
        dto.setId(carrito.getId());
        dto.setUsuarioId(carrito.getUsuarioId());
        dto.setEstado(carrito.getEstado());
        dto.setTotalEstimado(carrito.getTotalEstimado());
        dto.setNotasCliente(carrito.getNotasCliente());
        dto.setFechaExpiracion(carrito.getFechaExpiracion());
        dto.setCreadoEl(carrito.getCreadoEl());
        dto.setActualizadoEl(carrito.getActualizadoEl());
        
        if (carrito.getItems() != null) {
            dto.setItems(carrito.getItems().stream()
                .map(CarritoItemResponseDto::fromEntity)
                .collect(Collectors.toList()));
            dto.setTotalItems(carrito.getItems().size());
        } else {
            dto.setTotalItems(0);
        }
        
        return dto;
    }
}