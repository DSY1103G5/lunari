package cl.duoc.lunari.api.user.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper para paginación en DynamoDB.
 *
 * Reemplaza a Spring Data's Page<T> para trabajar con paginación de DynamoDB
 * que utiliza tokens en lugar de números de página.
 *
 * @param <T> Tipo de elementos en la página
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamoDbPage<T> {

    /**
     * Lista de items en esta página.
     */
    private List<T> items;

    /**
     * Token de paginación para obtener la siguiente página.
     * Null si no hay más páginas.
     */
    private String nextPaginationToken;

    /**
     * Indica si hay más resultados disponibles.
     */
    private boolean hasMoreResults;

    /**
     * Cantidad de items en esta página.
     */
    private int count;

    /**
     * Límite de items solicitados por página.
     */
    private int limit;

    /**
     * Constructor para crear una página sin más resultados.
     *
     * @param items Items de la página
     * @param limit Límite por página
     */
    public DynamoDbPage(List<T> items, int limit) {
        this.items = items;
        this.count = items != null ? items.size() : 0;
        this.limit = limit;
        this.hasMoreResults = false;
        this.nextPaginationToken = null;
    }

    /**
     * Constructor para crear una página con posibilidad de más resultados.
     *
     * @param items Items de la página
     * @param nextPaginationToken Token para la siguiente página
     * @param limit Límite por página
     */
    public DynamoDbPage(List<T> items, String nextPaginationToken, int limit) {
        this.items = items;
        this.count = items != null ? items.size() : 0;
        this.limit = limit;
        this.nextPaginationToken = nextPaginationToken;
        this.hasMoreResults = nextPaginationToken != null && !nextPaginationToken.isEmpty();
    }

    /**
     * Verifica si la página está vacía.
     *
     * @return true si no hay items
     */
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /**
     * Obtiene el número de items en la página.
     *
     * @return Cantidad de items
     */
    public int getSize() {
        return count;
    }
}
