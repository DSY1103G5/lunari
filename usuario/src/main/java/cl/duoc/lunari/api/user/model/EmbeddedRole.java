package cl.duoc.lunari.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Rol embebido en el objeto User.
 *
 * Reemplaza la relación con la tabla Rol de PostgreSQL.
 * Cada usuario almacena directamente su información de rol.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class EmbeddedRole {

    private Integer roleId;
    private String roleName;
    private String roleDescription;

    @DynamoDbAttribute("roleId")
    public Integer getRoleId() {
        return roleId;
    }

    @DynamoDbAttribute("roleName")
    public String getRoleName() {
        return roleName;
    }

    @DynamoDbAttribute("roleDescription")
    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }
}
