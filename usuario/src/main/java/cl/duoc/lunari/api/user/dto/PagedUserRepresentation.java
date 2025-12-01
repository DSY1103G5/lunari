package cl.duoc.lunari.api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

/**
 * DTO para representación de usuarios paginados.
 *
 * Cambios desde JPA:
 * - Paginación basada en tokens en lugar de números de página
 * - PageInfo actualizado para DynamoDB (sin totalPages/totalElements)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedUserRepresentation extends RepresentationModel<PagedUserRepresentation> {

    private List<UserRepresentation> users;
    private PageInfo page;

    /**
     * Información de paginación para DynamoDB (basada en tokens).
     *
     * NOTA: DynamoDB no proporciona totalElements/totalPages sin hacer scan completo,
     * por lo que solo incluimos información sobre la página actual.
     */
    @Data
    public static class PageInfo {
        private int count;                    // Número de elementos en la página actual
        private int limit;                    // Límite solicitado
        private String nextPaginationToken;   // Token para la siguiente página (null si es la última)
        private boolean hasMore;              // true si hay más resultados disponibles
    }
}
