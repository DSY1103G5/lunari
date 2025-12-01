package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Cupón de descuento del cliente.
 *
 * Representa un cupón de descuento asignado al cliente, ya sea de tipo
 * fijo (monto específico) o porcentual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Coupon {

    private String id;              // ID único del cupón (ej: COUP-001)
    private String code;            // Código del cupón (ej: BRONZE-ALX-001)
    private String description;     // Descripción del cupón
    private String type;            // Tipo de descuento: "fixed" o "percentage"
    private Double value;           // Valor del descuento (monto o porcentaje)
    private Double minPurchase;     // Compra mínima requerida para usar el cupón
    private String expiresAt;       // Fecha de expiración (ISO 8601 o YYYY-MM-DD)
    private Boolean isUsed = false; // Indica si el cupón ya fue usado

    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbAttribute("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDbAttribute("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @DynamoDbAttribute("value")
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @DynamoDbAttribute("minPurchase")
    public Double getMinPurchase() {
        return minPurchase;
    }

    public void setMinPurchase(Double minPurchase) {
        this.minPurchase = minPurchase;
    }

    @DynamoDbAttribute("expiresAt")
    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    @DynamoDbAttribute("isUsed")
    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    /**
     * Verifica si el cupón está vigente (no usado y no expirado).
     *
     * @return true si el cupón es válido
     */
    public boolean isValid() {
        return !Boolean.TRUE.equals(isUsed);
        // TODO: Add expiration date validation when needed
    }

    /**
     * Marca el cupón como usado.
     */
    public void markAsUsed() {
        this.isUsed = true;
    }
}
