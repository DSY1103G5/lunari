package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estadísticas de reseñas del usuario.
 *
 * Almacena información agregada sobre reseñas:
 * - Cantidad total de reseñas escritas
 * - Calificación promedio otorgada por el usuario
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStats {

    private Integer totalCount = 0;
    private Double averageRating; // null si no hay reseñas
}
