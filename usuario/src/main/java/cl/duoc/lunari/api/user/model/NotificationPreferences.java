package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preferencias de notificaciones del usuario.
 *
 * Controla qué canales de notificación están habilitados.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {

    private Boolean email = true;
    private Boolean sms = false;
    private Boolean push = true;

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
