package cl.duoc.lunari.api.user.dto;

import lombok.Data;

/**
 * DTO para el endpoint raíz de la API con enlaces de navegación HATEOAS.
 *
 * Cambios desde JPA:
 * - Removido getAllRoles (ahora en RoleController)
 * - Agregado getRoles (nuevo endpoint en RoleController)
 */
@Data
public class ApiRootDto {
    private String message;
    private ApiLinksDto links;

    public ApiRootDto() {
        this.message = "User API - HATEOAS habilitado (DynamoDB)";
        this.links = new ApiLinksDto();
    }

    @Data
    public static class ApiLinksDto {
        // User endpoints
        private String getAllUsers;
        private String getPaginatedUsers;
        private String searchUsers;
        private String getUserById;
        private String getUserByEmail;
        private String registerUser;
        private String getUserStats;

        // Role endpoints
        private String getRoles;
    }
}
