package cl.duoc.lunari.api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRepresentation extends RepresentationModel<UserRepresentation> {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImage;
    private Integer roleId;
    private UUID companyId;
    private Boolean active;
    private OffsetDateTime lastLogin;
    private Boolean verified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Computed properties for better user experience
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : lastName;
    }
    
    public String getStatus() {
        if (active != null && verified != null) {
            if (active && verified) {
                return "active";
            } else if (active && !verified) {
                return "pending_verification";
            } else {
                return "inactive";
            }
        }
        return "unknown";
    }
}
