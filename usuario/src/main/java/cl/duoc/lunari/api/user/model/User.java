package cl.duoc.lunari.api.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad User (Cliente) para PostgreSQL.
 *
 * Representa un cliente en la plataforma e-commerce LUNARi con información completa
 * de perfil personal, dirección, preferencias gaming, estadísticas y cupones.
 *
 * Tabla: users
 * Primary Key: id (UUID)
 * Indexes: email (unique), username (unique)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_username", columnList = "username", unique = true)
})
public class User {

    // ==================== Identificación ====================

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    private String id; // ID del usuario (UUID)

    @NotBlank(message = "Username no puede estar vacío")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @Schema(example = "omunoz")
    private String username; // Nombre de usuario único

    @Email
    @NotBlank(message = "Email no puede estar vacío")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    @Schema(example = "osca.munozs@duocuc.cl")
    private String email;

    @NotBlank(message = "Contraseña no puede estar vacía")
    @Size(min = 8, max = 255, message = "Contraseña debe tener entre 8 y 255 caracteres")
    @Column(name = "password", nullable = false)
    @Schema(example = "hashedPassword123")
    private String password;

    // ==================== Información del Cliente (JSON) ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "personal", columnDefinition = "jsonb")
    private Personal personal;              // Información personal (nombre, teléfono, bio, etc.)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address", columnDefinition = "jsonb")
    private Address address;                // Dirección de envío

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private ClientPreferences preferences;  // Preferencias de notificaciones y categorías

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gaming", columnDefinition = "jsonb")
    private Gaming gaming;                  // Perfil gaming del usuario

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats", columnDefinition = "jsonb")
    private ClientStats stats;              // Estadísticas (nivel, puntos, compras, etc.)

    // ==================== Cupones ====================

    @Convert(converter = cl.duoc.lunari.api.user.converter.CouponListConverter.class)
    @Column(name = "coupons", columnDefinition = "jsonb")
    private List<Coupon> coupons;           // Cupones del cliente

    // ==================== Estado y Verificación ====================

    @Column(name = "is_active", nullable = false)
    @Schema(example = "true")
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Schema(example = "true")
    private Boolean isVerified = false;

    // ==================== Timestamps ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(example = "2025-01-01T12:00:00")
    private LocalDateTime createdAt; // Timestamp de creación

    @Column(name = "updated_at", nullable = false)
    @Schema(example = "2025-07-04T14:30:00")
    private LocalDateTime updatedAt; // Timestamp de última actualización

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Helper Methods ====================

    /**
     * Obtiene el nombre completo del cliente.
     *
     * @return firstName + lastName del objeto personal
     */
    public String getFullName() {
        return personal != null ? personal.getFullName() : "";
    }

    /**
     * Verifica si el cliente está activo y verificado.
     *
     * @return true si está activo Y verificado
     */
    public boolean isActiveAndVerified() {
        return Boolean.TRUE.equals(isActive) && Boolean.TRUE.equals(isVerified);
    }

    /**
     * Obtiene el nivel del cliente desde las estadísticas.
     *
     * @return Nivel del cliente (Bronze, Silver, Gold, etc.)
     */
    public String getLevel() {
        return stats != null ? stats.getLevel() : "Bronze";
    }

    /**
     * Obtiene los puntos del cliente desde las estadísticas.
     *
     * @return Puntos de fidelidad
     */
    public Long getPoints() {
        return stats != null ? stats.getPoints() : 0L;
    }
}
