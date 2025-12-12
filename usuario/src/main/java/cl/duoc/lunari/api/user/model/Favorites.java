package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicios favoritos del usuario.
 *
 * Almacena una lista de IDs de servicios marcados como favoritos
 * y el conteo total.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorites {

    private List<Integer> serviceIds = new ArrayList<>();
    private Integer count = 0;

    /**
     * Agrega un servicio a favoritos si no existe.
     *
     * @param serviceId ID del servicio
     * @return true si se agregó, false si ya existía
     */
    public boolean addService(Integer serviceId) {
        if (serviceIds == null) {
            serviceIds = new ArrayList<>();
        }
        if (!serviceIds.contains(serviceId)) {
            serviceIds.add(serviceId);
            count = serviceIds.size();
            return true;
        }
        return false;
    }

    /**
     * Remueve un servicio de favoritos.
     *
     * @param serviceId ID del servicio
     * @return true si se removió, false si no existía
     */
    public boolean removeService(Integer serviceId) {
        if (serviceIds != null && serviceIds.remove(serviceId)) {
            count = serviceIds.size();
            return true;
        }
        return false;
    }
}
