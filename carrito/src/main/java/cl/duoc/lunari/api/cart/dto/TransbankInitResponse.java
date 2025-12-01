package cl.duoc.lunari.api.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta de Transbank al iniciar una transacción
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransbankInitResponse {

    private String token;
    private String url;
}
