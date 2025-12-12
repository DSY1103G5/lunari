package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadatos del usuario.
 *
 * Información adicional para tracking, analytics y segmentación:
 * - Última dirección IP utilizada
 * - Información del dispositivo
 * - Fuente de referido (marketing)
 * - Tags para segmentación
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {

    private String lastIpAddress;
    private String deviceInfo;
    private String referralSource; // utm_source, referrer, etc.
    private List<String> tags = new ArrayList<>(); // Tags para segmentación

    /**
     * Agrega un tag si no existe.
     *
     * @param tag Tag a agregar
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Remueve un tag.
     *
     * @param tag Tag a remover
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
}
