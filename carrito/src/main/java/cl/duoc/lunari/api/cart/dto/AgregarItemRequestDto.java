package cl.duoc.lunari.api.cart.dto;

import lombok.Data;

@Data
public class AgregarItemRequestDto {
    private Integer servicioId;
    private Integer cantidad;
    private String personalizaciones;
}