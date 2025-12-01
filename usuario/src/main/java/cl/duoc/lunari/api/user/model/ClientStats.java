package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Estadísticas del cliente en la plataforma.
 *
 * Contiene información sobre el nivel del cliente, puntos de fidelidad,
 * cantidad de compras, reseñas y favoritos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ClientStats {

    private String level = "Bronze";    // Nivel del cliente (Bronze, Silver, Gold, Platinum, etc.)
    private Long points = 0L;           // Puntos de fidelidad
    private Integer purchases = 0;      // Número de compras realizadas
    private Integer reviews = 0;        // Número de reseñas escritas
    private Integer favorites = 0;      // Número de productos en favoritos

    @DynamoDbAttribute("level")
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @DynamoDbAttribute("points")
    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    @DynamoDbAttribute("purchases")
    public Integer getPurchases() {
        return purchases;
    }

    public void setPurchases(Integer purchases) {
        this.purchases = purchases;
    }

    @DynamoDbAttribute("reviews")
    public Integer getReviews() {
        return reviews;
    }

    public void setReviews(Integer reviews) {
        this.reviews = reviews;
    }

    @DynamoDbAttribute("favorites")
    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    /**
     * Crea estadísticas por defecto para un nuevo cliente.
     *
     * @return ClientStats con valores iniciales
     */
    public static ClientStats createDefault() {
        return new ClientStats("Bronze", 0L, 0, 0, 0);
    }

    /**
     * Incrementa el contador de compras.
     */
    public void incrementPurchases() {
        this.purchases = (this.purchases != null ? this.purchases : 0) + 1;
    }

    /**
     * Incrementa el contador de reseñas.
     */
    public void incrementReviews() {
        this.reviews = (this.reviews != null ? this.reviews : 0) + 1;
    }

    /**
     * Incrementa el contador de favoritos.
     */
    public void incrementFavorites() {
        this.favorites = (this.favorites != null ? this.favorites : 0) + 1;
    }

    /**
     * Añade puntos al total del cliente.
     *
     * @param pointsToAdd Puntos a añadir
     */
    public void addPoints(Long pointsToAdd) {
        this.points = (this.points != null ? this.points : 0L) + pointsToAdd;
    }
}
