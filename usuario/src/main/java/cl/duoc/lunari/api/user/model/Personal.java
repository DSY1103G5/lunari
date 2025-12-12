package cl.duoc.lunari.api.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Información personal del cliente.
 *
 * Contiene datos personales básicos del usuario como nombre, teléfono,
 * fecha de nacimiento, biografía y foto de perfil.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Personal {

    private String firstName;
    private String lastName;
    private String phone;
    private String birthdate; // ISO 8601 format: YYYY-MM-DD
    private String bio;
    private String avatar; // URL to profile image
    private String memberSince; // Year or date when user joined (e.g., "2022")

    /**
     * Obtiene el nombre completo del cliente.
     *
     * @return firstName + lastName
     */
    @JsonIgnore
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
