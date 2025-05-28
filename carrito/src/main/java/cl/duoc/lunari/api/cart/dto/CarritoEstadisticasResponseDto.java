package cl.duoc.lunari.api.cart.dto;

import cl.duoc.lunari.api.cart.service.CarritoService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoEstadisticasResponseDto {
    private long carritoActivos;
    private long carritosProcesados;
    private long carritosAbandonados;
    private long carritosExpirados;
    private long totalCarritos;

    public static CarritoEstadisticasResponseDto fromStats(CarritoService.CarritoEstadisticas stats) {
        CarritoEstadisticasResponseDto dto = new CarritoEstadisticasResponseDto();
        dto.setCarritoActivos(stats.carritoActivos());
        dto.setCarritosProcesados(stats.carritosProcesados());
        dto.setCarritosAbandonados(stats.carritosAbandonados());
        dto.setCarritosExpirados(stats.carritosExpirados());
        dto.setTotalCarritos(stats.carritoActivos() + stats.carritosProcesados() + 
                           stats.carritosAbandonados() + stats.carritosExpirados());
        return dto;
    }
}