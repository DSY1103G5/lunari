package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cupón de descuento del cliente.
 *
 * Representa un cupón de descuento asignado al cliente, ya sea de tipo
 * fijo (monto específico) o porcentual.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    private String id;              // ID único del cupón (ej: COUP-001)
    private String code;            // Código del cupón (ej: BRONZE-ALX-001)
    private String description;     // Descripción del cupón
    private String type;            // Tipo de descuento: "fixed" o "percentage"
    private Double value;           // Valor del descuento (monto o porcentaje)
    private Double minPurchase;     // Compra mínima requerida para usar el cupón
    private String expiresAt;       // Fecha de expiración (ISO 8601 o YYYY-MM-DD)
    private Boolean isUsed = false; // Indica si el cupón ya fue usado

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
