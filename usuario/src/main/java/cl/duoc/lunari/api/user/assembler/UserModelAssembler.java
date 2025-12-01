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

/**
 * Assembler para convertir entidades User (clientes) a UserRepresentation con HATEOAS links.
 *
 * Enlaces enfocados en e-commerce:
 * - Gestión de perfil del cliente
 * - Cupones
 * - Preferencias y dirección
 * - Perfil gaming
 * - Estadísticas
 *
 * Removido:
 * - Enlaces de roles (los clientes no tienen roles)
 * - Enlaces de administración
 * - Enlaces de metadata
 */
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

        // Self link - Perfil del cliente
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .getUserById(user.getId())).withSelfRel());

        // Collection link - Todos los clientes
        userRepresentation.add(linkTo(UserController.class).withRel("clients"));

        // Update link - Actualizar perfil
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .updateUser(user.getId(), null)).withRel("update"));

        // Delete link - Eliminar cuenta
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .deleteUser(user.getId())).withRel("delete"));

        // Password change link - Cambiar contraseña
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .updateUserPassword(user.getId(), null)).withRel("change-password"));

        // Status management links - Activar/Desactivar cuenta
        if (user.getIsActive() != null) {
            if (user.getIsActive()) {
                userRepresentation.add(linkTo(methodOn(UserController.class)
                        .updateUserStatus(user.getId(), false)).withRel("deactivate"));
            } else {
                userRepresentation.add(linkTo(methodOn(UserController.class)
                        .updateUserStatus(user.getId(), true)).withRel("activate"));
            }
        }

        // Gamification links - Puntos de fidelidad
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .addPoints(user.getId(), null)).withRel("add-points"));

        // Favorites link - Favoritos (contador)
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .addFavorite(user.getId(), null)).withRel("add-favorite"));

        // Purchase tracking link - Registrar compra
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .recordPurchase(user.getId(), null)).withRel("record-purchase"));

        // Review tracking link - Registrar reseña
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .recordReview(user.getId(), null)).withRel("record-review"));

        // Address link - Actualizar dirección
        userRepresentation.add(linkTo(methodOn(UserController.class)
                .updateAddress(user.getId(), null)).withRel("update-address"));

        // E-commerce specific links - Enlaces de e-commerce

        // Cupones - Solo si el cliente tiene cupones o puede recibir más
        if (user.getCoupons() != null && !user.getCoupons().isEmpty()) {
            // Link to view coupons (futuro endpoint)
            // userRepresentation.add(linkTo(methodOn(UserController.class)
            //         .getClientCoupons(user.getId())).withRel("coupons"));
        }

        // Gaming profile - Perfil gaming
        // userRepresentation.add(linkTo(methodOn(UserController.class)
        //         .updateGamingProfile(user.getId(), null)).withRel("update-gaming"));

        // Preferences - Preferencias de cliente
        // userRepresentation.add(linkTo(methodOn(UserController.class)
        //         .updateClientPreferences(user.getId(), null)).withRel("update-preferences"));

        // Stats - Estadísticas del cliente
        // userRepresentation.add(linkTo(methodOn(UserController.class)
        //         .getClientStats(user.getId())).withRel("stats"));

        return userRepresentation;
    }
}
