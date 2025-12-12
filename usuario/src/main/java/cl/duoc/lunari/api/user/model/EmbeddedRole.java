package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rol embebido en el objeto User.
 *
 * Almacena información de rol directamente en el usuario.
 *
 * Esta clase se serializa como JSON en PostgreSQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddedRole {

    private Integer roleId;
    private String roleName;
    private String roleDescription;
}
