package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Estadísticas de compras del usuario.
 *
 * Almacena información agregada sobre el historial de compras:
 * - Cantidad total de compras realizadas
 * - Monto total gastado (almacenado como String para precisión decimal)
 * - Fecha de última compra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class PurchaseStats {

    private Integer totalCount = 0;
    private String totalSpent = "0.00"; // Decimal como String para precisión
    private String lastPurchaseDate; // ISO 8601 timestamp

    @DynamoDbAttribute("totalCount")
    public Integer getTotalCount() {
        return totalCount;
    }

    @DynamoDbAttribute("totalSpent")
    public String getTotalSpent() {
        return totalSpent;
    }

    @DynamoDbAttribute("lastPurchaseDate")
    public String getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public void setTotalSpent(String totalSpent) {
        this.totalSpent = totalSpent;
    }

    public void setLastPurchaseDate(String lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
}
