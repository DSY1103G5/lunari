package cl.duoc.lunari.api.user.dto;

import lombok.Data;

@Data
public class ApiRootDto {
    private String message;
    private ApiLinksDto links;
    
    public ApiRootDto() {
        this.message = "User API - HATEOAS habilitado";
        this.links = new ApiLinksDto();
    }
    
    @Data
    public static class ApiLinksDto {
        private String getAllUsers;
        private String getPaginatedUsers;
        private String searchUsers;
        private String getUserById;
        private String getUserByEmail;
        private String registerUser;
        private String getAllRoles;
        private String getUserStats;
    }
}
