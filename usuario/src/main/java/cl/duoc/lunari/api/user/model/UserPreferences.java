package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preferencias del usuario.
 *
 * Configuraciones personalizables como idioma, moneda y notificaciones.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {

    private String language = "es"; // Idioma (es, en, etc.)
    private String currency = "CLP"; // Moneda (CLP, USD, etc.)
    private NotificationPreferences notifications;

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
