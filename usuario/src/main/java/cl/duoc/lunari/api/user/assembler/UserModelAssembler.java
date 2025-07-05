package cl.duoc.lunari.api.user.assembler;

import cl.duoc.lunari.api.user.controller.UserController;
import cl.duoc.lunari.api.user.dto.UserMapper;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserModelAssembler extends RepresentationModelAssemblerSupport<User, UserRepresentation> {

    @Autowired
    private UserMapper userMapper;

    public UserModelAssembler() {
        super(UserController.class, UserRepresentation.class);
    }

    @Override
    @NonNull
    public UserRepresentation toModel(@NonNull User user) {
        UserRepresentation userRepresentation = userMapper.toRepresentation(user);
        
        // Self link
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .getUserById(user.getId())).withSelfRel());
        
        // Collection link
        userRepresentation.add(linkTo(UserController.class).withRel("users"));
        
        // Update link
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .updateUser(user.getId(), null)).withRel("update"));
        
        // Delete link
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .deleteUser(user.getId())).withRel("delete"));
        
        // Status management links
        if (user.getActive() != null) {
            if (user.getActive()) {
                userRepresentation.add(linkTo(methodOn(UserController.class)
                        .updateUserStatus(user.getId(), false)).withRel("deactivate"));
            } else {
                userRepresentation.add(linkTo(methodOn(UserController.class)
                        .updateUserStatus(user.getId(), true)).withRel("activate"));
            }
        }
        
        // Password change link
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .updateUserPassword(user.getId(), null)).withRel("change-password"));
        
        // Company-related links
        if (user.getCompanyId() != null) {
            userRepresentation.add(linkTo(methodOn(UserController.class)
                    .getUsersByCompany(user.getCompanyId(), 0, 10)).withRel("company-users"));
        }
        
        // Role-related links
        if (user.getRoleId() != null) {
            userRepresentation.add(linkTo(methodOn(UserController.class)
                    .getUsersByRole(user.getRoleId(), 0, 10)).withRel("role-users"));
            userRepresentation.add(linkTo(methodOn(UserController.class)
                    .assignRoleToUser(user.getId(), null)).withRel("assign-role"));
        }
        
        // Verification link (if not verified)
        if (user.getVerified() != null && !user.getVerified()) {
            userRepresentation.add(linkTo(methodOn(UserController.class)
                    .verifyUser(null)).withRel("verify"));
        }
        
        return userRepresentation;
    }
}
