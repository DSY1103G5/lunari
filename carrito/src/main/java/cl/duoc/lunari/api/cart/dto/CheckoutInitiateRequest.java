package cl.duoc.lunari.api.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para iniciar el proceso de checkout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutInitiateRequest {

    @NotNull(message = "ID del carrito no puede estar vacío")
    private UUID carritoId;

    @NotNull(message = "URL de retorno no puede estar vacía")
    private String returnUrl;

    private String notasCliente;
}
