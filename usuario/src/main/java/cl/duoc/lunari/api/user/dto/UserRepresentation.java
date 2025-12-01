package cl.duoc.lunari.api.user.dto;

import cl.duoc.lunari.api.user.model.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

/**
 * DTO para representación de clientes en respuestas de API.
 *
 * Representa un cliente de e-commerce con toda su información:
 * - Datos personales agrupados en "personal"
 * - Dirección de envío
 * - Preferencias de gaming y notificaciones
 * - Perfil gaming
 * - Estadísticas de fidelidad
 * - Cupones disponibles
 *
 * Nota: La contraseña NUNCA se incluye en las respuestas.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRepresentation extends RepresentationModel<UserRepresentation> {

    // ==================== Identificación ====================
    private String id;
    private String username;
    private String email;

    // ==================== Información del Cliente ====================
    private Personal personal;              // Datos personales (nombre, teléfono, bio, avatar, etc.)
    private Address address;                // Dirección de envío
    private ClientPreferences preferences;  // Preferencias (categorías, notificaciones, etc.)
    private Gaming gaming;                  // Perfil gaming
    private ClientStats stats;              // Estadísticas (nivel, puntos, compras, reviews, favoritos)

    // ==================== Cupones ====================
    private List<Coupon> coupons;           // Cupones disponibles

    // ==================== Estado ====================
    private Boolean isActive;
    private Boolean isVerified;

    // ==================== Timestamps ====================
    private String createdAt;
    private String updatedAt;

    // ==================== Computed Properties ====================

    /**
     * Obtiene el nombre completo del cliente.
     *
     * @return firstName + lastName desde personal
     */
    public String getFullName() {
        return personal != null ? personal.getFullName() : "";
    }

    /**
     * Obtiene el estado del cliente como texto.
     *
     * @return Estado: "active", "pending_verification" o "inactive"
     */
    public String getStatus() {
        if (Boolean.TRUE.equals(isActive) && Boolean.TRUE.equals(isVerified)) {
            return "active";
        } else if (Boolean.TRUE.equals(isActive) && !Boolean.TRUE.equals(isVerified)) {
            return "pending_verification";
        } else {
            return "inactive";
        }
    }

    /**
     * Obtiene el nivel del cliente desde las estadísticas.
     *
     * @return Nivel (Bronze, Silver, Gold, etc.)
     */
    public String getLevel() {
        return stats != null ? stats.getLevel() : "Bronze";
    }

    /**
     * Obtiene los puntos de fidelidad del cliente.
     *
     * @return Puntos acumulados
     */
    public Long getPoints() {
        return stats != null ? stats.getPoints() : 0L;
    }

    /**
     * Cuenta cupones activos (no usados).
     *
     * @return Número de cupones disponibles
     */
    public int getActiveCouponsCount() {
        if (coupons == null || coupons.isEmpty()) {
            return 0;
        }
        return (int) coupons.stream()
                .filter(c -> !Boolean.TRUE.equals(c.getIsUsed()))
                .count();
    }
}
