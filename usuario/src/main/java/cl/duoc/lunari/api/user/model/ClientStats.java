package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estadísticas del cliente en la plataforma.
 *
 * Contiene información sobre el nivel del cliente, puntos de fidelidad,
 * cantidad de compras, reseñas y favoritos.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientStats {

    private String level = "Bronze";    // Nivel del cliente (Bronze, Silver, Gold, Platinum, etc.)
    private Long points = 0L;           // Puntos de fidelidad
    private Integer purchases = 0;      // Número de compras realizadas
    private Integer reviews = 0;        // Número de reseñas escritas
    private Integer favorites = 0;      // Número de productos en favoritos

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
