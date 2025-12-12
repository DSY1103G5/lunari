package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Preferencias del cliente en la plataforma de e-commerce.
 *
 * Contiene configuraciones de notificaciones, categorías favoritas,
 * plataforma preferida y horarios de juego.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPreferences {

    private List<String> favoriteCategories;  // Categorías favoritas (JM, CG, AC, etc.)
    private String preferredPlatform;         // Plataforma preferida (pc, ps5, xbox, switch, etc.)
    private String gamingHours;               // Horas de juego semanales (0-5, 6-10, 11-15, 16-30, 30+)

    // Preferencias de notificaciones
    private Boolean notifyOffers = true;        // Notificar ofertas especiales
    private Boolean notifyNewProducts = true;   // Notificar nuevos productos
    private Boolean notifyRestocks = false;     // Notificar reposiciones
    private Boolean notifyNewsletter = true;    // Suscribirse a newsletter

    /**
     * Crea preferencias por defecto para un nuevo cliente.
     *
     * @return ClientPreferences con valores por defecto
     */
    public static ClientPreferences createDefault() {
        ClientPreferences prefs = new ClientPreferences();
        prefs.setNotifyOffers(true);
        prefs.setNotifyNewProducts(true);
        prefs.setNotifyRestocks(false);
        prefs.setNotifyNewsletter(true);
        return prefs;
    }
}
