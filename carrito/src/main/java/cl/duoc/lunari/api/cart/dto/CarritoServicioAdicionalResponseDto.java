package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.model.CarritoServicioAdicional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoServicioAdicionalResponseDto {
    private UUID id;
    private UUID carritoItemId;
    private Integer servicioAdicionalId;
    private BigDecimal precioAdicional;
    private OffsetDateTime creadoEl;

    public static CarritoServicioAdicionalResponseDto fromEntity(CarritoServicioAdicional servicioAdicional) {
        CarritoServicioAdicionalResponseDto dto = new CarritoServicioAdicionalResponseDto();
        dto.setId(servicioAdicional.getId());
        dto.setCarritoItemId(servicioAdicional.getCarritoItem().getId());
        dto.setServicioAdicionalId(servicioAdicional.getServicioAdicionalId());
        dto.setPrecioAdicional(servicioAdicional.getPrecioAdicional());
        dto.setCreadoEl(servicioAdicional.getCreadoEl());
        return dto;
    }
}