package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.model.CarritoItem;
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
public class CarritoItemResponseDto {
    private UUID id;
    private UUID carritoId;
    private Integer servicioId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String personalizaciones;
    private OffsetDateTime creadoEl;
    private OffsetDateTime actualizadoEl;
    private List<CarritoServicioAdicionalResponseDto> serviciosAdicionales;

    public static CarritoItemResponseDto fromEntity(CarritoItem item) {
        CarritoItemResponseDto dto = new CarritoItemResponseDto();
        dto.setId(item.getId());
        dto.setCarritoId(item.getCarrito().getId());
        dto.setServicioId(item.getServicioId());
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        dto.setPersonalizaciones(item.getPersonalizaciones());
        dto.setCreadoEl(item.getCreadoEl());
        dto.setActualizadoEl(item.getActualizadoEl());
        
        if (item.getServiciosAdicionales() != null) {
            dto.setServiciosAdicionales(item.getServiciosAdicionales().stream()
                .map(CarritoServicioAdicionalResponseDto::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
}