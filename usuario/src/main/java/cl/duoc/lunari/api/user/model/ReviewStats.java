package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Estadísticas de reseñas del usuario.
 *
 * Almacena información agregada sobre reseñas:
 * - Cantidad total de reseñas escritas
 * - Calificación promedio otorgada por el usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ReviewStats {

    private Integer totalCount = 0;
    private Double averageRating; // null si no hay reseñas

    @DynamoDbAttribute("totalCount")
    public Integer getTotalCount() {
        return totalCount;
    }

    @DynamoDbAttribute("averageRating")
    public Double getAverageRating() {
        return averageRating;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
