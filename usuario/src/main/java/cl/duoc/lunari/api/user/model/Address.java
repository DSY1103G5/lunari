package cl.duoc.lunari.api.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dirección de envío del cliente.
 *
 * Información de ubicación física del cliente para envíos y facturación.
 * Incluye líneas de dirección, ciudad, región, código postal, país y
 * notas de entrega.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String addressLine1;    // Calle principal y número
    private String addressLine2;    // Departamento, oficina, etc. (opcional)
    private String city;            // Ciudad
    private String region;          // Región o estado
    private String postalCode;      // Código postal
    private String country;         // País (ej: "chile", "CL")
    private String deliveryNotes;   // Notas especiales de entrega

    /**
     * Obtiene la dirección completa formateada.
     *
     * @return Dirección en formato legible
     */
    @JsonIgnore
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.isEmpty()) sb.append(", ").append(addressLine2);
        if (city != null) sb.append(", ").append(city);
        if (region != null) sb.append(", ").append(region);
        if (postalCode != null) sb.append(" ").append(postalCode);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }
}
