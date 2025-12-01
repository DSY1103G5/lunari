package cl.duoc.lunari.api.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Entidad User (Cliente) para DynamoDB.
 *
 * Representa un cliente en la plataforma e-commerce LUNARi con información completa
 * de perfil personal, dirección, preferencias gaming, estadísticas y cupones.
 *
 * Tabla: lunari-users
 * Primary Key: id (Partition Key) - ID numérico o UUID como String
 * GSI #1: EmailIndex (email como PK) - Para autenticación
 * GSI #2: UsernameIndex (username como PK) - Para búsqueda por username
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {

    // ==================== Identificación ====================

    @Schema(example = "1")
    private String id; // ID del usuario (puede ser numérico o UUID)

    @NotBlank(message = "Username no puede estar vacío")
    @Schema(example = "omunoz")
    private String username; // Nombre de usuario único

    @Email
    @NotBlank(message = "Email no puede estar vacío")
    @Schema(example = "osca.munozs@duocuc.cl")
    private String email;

    @NotBlank(message = "Contraseña no puede estar vacía")
    @Size(min = 8, max = 64, message = "Contraseña debe tener entre 8 y 64 caracteres")
    @Schema(example = "hashedPassword123")
    private String password;

    // ==================== Información del Cliente ====================

    private Personal personal;              // Información personal (nombre, teléfono, bio, etc.)
    private Address address;                // Dirección de envío
    private ClientPreferences preferences;  // Preferencias de notificaciones y categorías
    private Gaming gaming;                  // Perfil gaming del usuario
    private ClientStats stats;              // Estadísticas (nivel, puntos, compras, etc.)

    // ==================== Cupones ====================

    private List<Coupon> coupons;           // Cupones del cliente

    // ==================== Estado y Verificación ====================

    @Schema(example = "true")
    private Boolean isActive = true;

    @Schema(example = "true")
    private Boolean isVerified = false;

    // ==================== Timestamps ====================

    @Schema(example = "2025-01-01T12:00:00Z")
    private String createdAt; // ISO 8601 timestamp

    @Schema(example = "2025-07-04T14:30:00Z")
    private String updatedAt; // ISO 8601 timestamp

    // ==================== DynamoDB Annotations ====================

    /**
     * Primary Key (Partition Key) - id.
     */
    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * GSI #1: EmailIndex - Partition Key.
     * Permite búsqueda rápida por email (autenticación).
     */
    @DynamoDbSecondaryPartitionKey(indexNames = "EmailIndex")
    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * GSI #2: UsernameIndex - Partition Key.
     * Permite búsqueda rápida por username.
     */
    @DynamoDbSecondaryPartitionKey(indexNames = "UsernameIndex")
    @DynamoDbAttribute("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // ==================== Getters y Setters con DynamoDbAttribute ====================

    @DynamoDbAttribute("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDbAttribute("personal")
    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    @DynamoDbAttribute("address")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @DynamoDbAttribute("preferences")
    public ClientPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(ClientPreferences preferences) {
        this.preferences = preferences;
    }

    @DynamoDbAttribute("gaming")
    public Gaming getGaming() {
        return gaming;
    }

    public void setGaming(Gaming gaming) {
        this.gaming = gaming;
    }

    @DynamoDbAttribute("stats")
    public ClientStats getStats() {
        return stats;
    }

    public void setStats(ClientStats stats) {
        this.stats = stats;
    }

    @DynamoDbAttribute("coupons")
    public List<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }

    @DynamoDbAttribute("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @DynamoDbAttribute("isVerified")
    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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
