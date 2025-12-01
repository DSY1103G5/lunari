package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicios favoritos del usuario.
 *
 * Almacena una lista de IDs de servicios marcados como favoritos
 * y el conteo total. Limitado a un máximo razonable (ej: 20-50 items)
 * para evitar que el item de DynamoDB crezca excesivamente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Favorites {

    private List<Integer> serviceIds = new ArrayList<>();
    private Integer count = 0;

    @DynamoDbAttribute("serviceIds")
    public List<Integer> getServiceIds() {
        return serviceIds;
    }

    @DynamoDbAttribute("count")
    public Integer getCount() {
        return count;
    }

    public void setServiceIds(List<Integer> serviceIds) {
        this.serviceIds = serviceIds;
        this.count = serviceIds != null ? serviceIds.size() : 0;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

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
