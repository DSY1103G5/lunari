package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Metadata {

    private String lastIpAddress;
    private String deviceInfo;
    private String referralSource; // utm_source, referrer, etc.
    private List<String> tags = new ArrayList<>(); // Tags para segmentación

    @DynamoDbAttribute("lastIpAddress")
    public String getLastIpAddress() {
        return lastIpAddress;
    }

    @DynamoDbAttribute("deviceInfo")
    public String getDeviceInfo() {
        return deviceInfo;
    }

    @DynamoDbAttribute("referralSource")
    public String getReferralSource() {
        return referralSource;
    }

    @DynamoDbAttribute("tags")
    public List<String> getTags() {
        return tags;
    }

    public void setLastIpAddress(String lastIpAddress) {
        this.lastIpAddress = lastIpAddress;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setReferralSource(String referralSource) {
        this.referralSource = referralSource;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

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
