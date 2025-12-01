package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Preferencias del usuario.
 *
 * Configuraciones personalizables como idioma, moneda y notificaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class UserPreferences {

    private String language = "es"; // Idioma (es, en, etc.)
    private String currency = "CLP"; // Moneda (CLP, USD, etc.)
    private NotificationPreferences notifications;

    @DynamoDbAttribute("language")
    public String getLanguage() {
        return language;
    }

    @DynamoDbAttribute("currency")
    public String getCurrency() {
        return currency;
    }

    @DynamoDbAttribute("notifications")
    public NotificationPreferences getNotifications() {
        return notifications;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setNotifications(NotificationPreferences notifications) {
        this.notifications = notifications;
    }

    /**
     * Crea preferencias por defecto para un nuevo usuario.
     *
     * @return UserPreferences con valores por defecto
     */
    public static UserPreferences createDefault() {
        UserPreferences prefs = new UserPreferences();
        prefs.setLanguage("es");
        prefs.setCurrency("CLP");
        prefs.setNotifications(NotificationPreferences.createDefault());
        return prefs;
    }
}
