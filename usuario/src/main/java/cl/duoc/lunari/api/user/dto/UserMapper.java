package cl.duoc.lunari.api.user.dto;

import cl.duoc.lunari.api.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserRepresentation toRepresentation(User user) {
        if (user == null) {
            return null;
        }
        
        UserRepresentation representation = new UserRepresentation();
        representation.setId(user.getId());
        representation.setFirstName(user.getFirstName());
        representation.setLastName(user.getLastName());
        representation.setEmail(user.getEmail());
        representation.setPhone(user.getPhone());
        representation.setProfileImage(user.getProfileImage());
        representation.setRoleId(user.getRoleId());
        representation.setCompanyId(user.getCompanyId());
        representation.setActive(user.getActive());
        representation.setLastLogin(user.getLastLogin());
        representation.setVerified(user.getVerified());
        representation.setCreatedAt(user.getCreatedAt());
        representation.setUpdatedAt(user.getUpdatedAt());
        
        return representation;
    }

    public User toEntity(UserRepresentation representation) {
        if (representation == null) {
            return null;
        }
        
        User user = new User();
        user.setId(representation.getId());
        user.setFirstName(representation.getFirstName());
        user.setLastName(representation.getLastName());
        user.setEmail(representation.getEmail());
        user.setPhone(representation.getPhone());
        user.setProfileImage(representation.getProfileImage());
        user.setRoleId(representation.getRoleId());
        user.setCompanyId(representation.getCompanyId());
        user.setActive(representation.getActive());
        user.setLastLogin(representation.getLastLogin());
        user.setVerified(representation.getVerified());
        user.setCreatedAt(representation.getCreatedAt());
        user.setUpdatedAt(representation.getUpdatedAt());
        
        return user;
    }
}
