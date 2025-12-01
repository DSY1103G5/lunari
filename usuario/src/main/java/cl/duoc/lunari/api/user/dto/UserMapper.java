package cl.duoc.lunari.api.user.dto;

import cl.duoc.lunari.api.user.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades User (clientes) y DTOs UserRepresentation.
 *
 * Mapea la estructura de e-commerce con nested objects:
 * - personal (Personal) - información personal
 * - address (Address) - dirección de envío
 * - preferences (ClientPreferences) - preferencias del cliente
 * - gaming (Gaming) - perfil gaming
 * - stats (ClientStats) - estadísticas de fidelidad
 * - coupons (List<Coupon>) - cupones del cliente
 *
 * Nota: La contraseña nunca se incluye en las representaciones.
 */
@Component
public class UserMapper {

    /**
     * Convierte un User entity a UserRepresentation DTO.
     *
     * @param user Entidad User
     * @return UserRepresentation DTO (sin contraseña)
     */
    public UserRepresentation toRepresentation(User user) {
        if (user == null) {
            return null;
        }

        UserRepresentation representation = new UserRepresentation();

        // Identificación
        representation.setId(user.getId());
        representation.setUsername(user.getUsername());
        representation.setEmail(user.getEmail());

        // Información del cliente (nested objects)
        representation.setPersonal(user.getPersonal());
        representation.setAddress(user.getAddress());
        representation.setPreferences(user.getPreferences());
        representation.setGaming(user.getGaming());
        representation.setStats(user.getStats());

        // Cupones
        representation.setCoupons(user.getCoupons());

        // Estado
        representation.setIsActive(user.getIsActive());
        representation.setIsVerified(user.getIsVerified());

        // Timestamps
        representation.setCreatedAt(user.getCreatedAt());
        representation.setUpdatedAt(user.getUpdatedAt());

        // Nota: La contraseña NO se incluye en la representación

        return representation;
    }

    /**
     * Convierte un UserRepresentation DTO a User entity.
     *
     * Usado principalmente para creación y actualización de clientes.
     * La contraseña debe ser manejada por separado por el servicio.
     *
     * @param representation UserRepresentation DTO
     * @return User entity
     */
    public User toEntity(UserRepresentation representation) {
        if (representation == null) {
            return null;
        }

        User user = new User();

        // Identificación
        user.setId(representation.getId());
        user.setUsername(representation.getUsername());
        user.setEmail(representation.getEmail());

        // Información del cliente (nested objects)
        user.setPersonal(representation.getPersonal());
        user.setAddress(representation.getAddress());
        user.setPreferences(representation.getPreferences());
        user.setGaming(representation.getGaming());
        user.setStats(representation.getStats());

        // Cupones
        user.setCoupons(representation.getCoupons());

        // Estado
        user.setIsActive(representation.getIsActive());
        user.setIsVerified(representation.getIsVerified());

        // Timestamps
        user.setCreatedAt(representation.getCreatedAt());
        user.setUpdatedAt(representation.getUpdatedAt());

        // Nota: La contraseña debe ser establecida por separado

        return user;
    }

    /**
     * Actualiza los campos editables de un User existente desde un DTO.
     *
     * No actualiza: id, email (inmutable), password, timestamps
     *
     * @param user User existente
     * @param representation DTO con datos actualizados
     */
    public void updateEntityFromRepresentation(User user, UserRepresentation representation) {
        if (user == null || representation == null) {
            return;
        }

        // Campos editables
        if (representation.getUsername() != null) {
            user.setUsername(representation.getUsername());
        }

        if (representation.getPersonal() != null) {
            user.setPersonal(representation.getPersonal());
        }

        if (representation.getAddress() != null) {
            user.setAddress(representation.getAddress());
        }

        if (representation.getPreferences() != null) {
            user.setPreferences(representation.getPreferences());
        }

        if (representation.getGaming() != null) {
            user.setGaming(representation.getGaming());
        }

        // Stats y cupones normalmente son manejados por el servicio,
        // no directamente por el cliente
    }
}
