package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Dirección de envío del cliente.
 *
 * Información de ubicación física del cliente para envíos y facturación.
 * Incluye líneas de dirección, ciudad, región, código postal, país y
 * notas de entrega.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Address {

    private String addressLine1;    // Calle principal y número
    private String addressLine2;    // Departamento, oficina, etc. (opcional)
    private String city;            // Ciudad
    private String region;          // Región o estado
    private String postalCode;      // Código postal
    private String country;         // País (ej: "chile", "CL")
    private String deliveryNotes;   // Notas especiales de entrega

    @DynamoDbAttribute("addressLine1")
    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    @DynamoDbAttribute("addressLine2")
    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    @DynamoDbAttribute("city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @DynamoDbAttribute("region")
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @DynamoDbAttribute("postalCode")
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @DynamoDbAttribute("country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @DynamoDbAttribute("deliveryNotes")
    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    /**
     * Obtiene la dirección completa formateada.
     *
     * @return Dirección en formato legible
     */
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
