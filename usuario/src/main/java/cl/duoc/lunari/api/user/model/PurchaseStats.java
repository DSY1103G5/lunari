package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estadísticas de compras del usuario.
 *
 * Almacena información agregada sobre el historial de compras:
 * - Cantidad total de compras realizadas
 * - Monto total gastado (almacenado como String para precisión decimal)
 * - Fecha de última compra
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseStats {

    private Integer totalCount = 0;
    private String totalSpent = "0.00"; // Decimal como String para precisión
    private String lastPurchaseDate; // ISO 8601 timestamp
}
