package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Preferencias de notificaciones del usuario.
 *
 * Controla qué canales de notificación están habilitados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class NotificationPreferences {

    private Boolean email = true;
    private Boolean sms = false;
    private Boolean push = true;

    @DynamoDbAttribute("email")
    public Boolean getEmail() {
        return email;
    }

    @DynamoDbAttribute("sms")
    public Boolean getSms() {
        return sms;
    }

    @DynamoDbAttribute("push")
    public Boolean getPush() {
        return push;
    }

    public void setEmail(Boolean email) {
        this.email = email;
    }

    public void setSms(Boolean sms) {
        this.sms = sms;
    }

    public void setPush(Boolean push) {
        this.push = push;
    }

    /**
     * Crea preferencias de notificación por defecto.
     *
     * @return NotificationPreferences con valores por defecto
     */
    public static NotificationPreferences createDefault() {
        NotificationPreferences prefs = new NotificationPreferences();
        prefs.setEmail(true);
        prefs.setSms(false);
        prefs.setPush(true);
        return prefs;
    }
}
