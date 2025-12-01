package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

/**
 * Preferencias del cliente en la plataforma de e-commerce.
 *
 * Contiene configuraciones de notificaciones, categorías favoritas,
 * plataforma preferida y horarios de juego.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class ClientPreferences {

    private List<String> favoriteCategories;  // Categorías favoritas (JM, CG, AC, etc.)
    private String preferredPlatform;         // Plataforma preferida (pc, ps5, xbox, switch, etc.)
    private String gamingHours;               // Horas de juego semanales (0-5, 6-10, 11-15, 16-30, 30+)

    // Preferencias de notificaciones
    private Boolean notifyOffers = true;        // Notificar ofertas especiales
    private Boolean notifyNewProducts = true;   // Notificar nuevos productos
    private Boolean notifyRestocks = false;     // Notificar reposiciones
    private Boolean notifyNewsletter = true;    // Suscribirse a newsletter

    @DynamoDbAttribute("favoriteCategories")
    public List<String> getFavoriteCategories() {
        return favoriteCategories;
    }

    public void setFavoriteCategories(List<String> favoriteCategories) {
        this.favoriteCategories = favoriteCategories;
    }

    @DynamoDbAttribute("preferredPlatform")
    public String getPreferredPlatform() {
        return preferredPlatform;
    }

    public void setPreferredPlatform(String preferredPlatform) {
        this.preferredPlatform = preferredPlatform;
    }

    @DynamoDbAttribute("gamingHours")
    public String getGamingHours() {
        return gamingHours;
    }

    public void setGamingHours(String gamingHours) {
        this.gamingHours = gamingHours;
    }

    @DynamoDbAttribute("notifyOffers")
    public Boolean getNotifyOffers() {
        return notifyOffers;
    }

    public void setNotifyOffers(Boolean notifyOffers) {
        this.notifyOffers = notifyOffers;
    }

    @DynamoDbAttribute("notifyNewProducts")
    public Boolean getNotifyNewProducts() {
        return notifyNewProducts;
    }

    public void setNotifyNewProducts(Boolean notifyNewProducts) {
        this.notifyNewProducts = notifyNewProducts;
    }

    @DynamoDbAttribute("notifyRestocks")
    public Boolean getNotifyRestocks() {
        return notifyRestocks;
    }

    public void setNotifyRestocks(Boolean notifyRestocks) {
        this.notifyRestocks = notifyRestocks;
    }

    @DynamoDbAttribute("notifyNewsletter")
    public Boolean getNotifyNewsletter() {
        return notifyNewsletter;
    }

    public void setNotifyNewsletter(Boolean notifyNewsletter) {
        this.notifyNewsletter = notifyNewsletter;
    }

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
