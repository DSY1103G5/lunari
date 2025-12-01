package cl.duoc.lunari.api.user.service;

import cl.duoc.lunari.api.user.model.EmbeddedRole;
import cl.duoc.lunari.api.user.util.RoleType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar roles predefinidos.
 *
 * Los roles ya no están en base de datos, sino que son constantes
 * definidas en el enum RoleType. Este servicio proporciona acceso
 * a esos roles predefinidos.
 */
@Service
public class RoleService {

    /**
     * Obtiene todos los roles disponibles.
     *
     * @return Lista de roles
     */
    public List<EmbeddedRole> getAllRoles() {
        return Arrays.stream(RoleType.values())
            .map(RoleType::toEmbeddedRole)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene un rol por su ID.
     *
     * @param roleId ID del rol
     * @return Optional con el rol si existe
     */
    public Optional<EmbeddedRole> getRoleById(Integer roleId) {
        return RoleType.fromId(roleId)
            .map(RoleType::toEmbeddedRole);
    }

    /**
     * Obtiene un rol por su nombre.
     *
     * @param roleName Nombre del rol
     * @return Optional con el rol si existe
     */
    public Optional<EmbeddedRole> getRoleByName(String roleName) {
        return RoleType.fromName(roleName)
            .map(RoleType::toEmbeddedRole);
    }

    /**
     * Verifica si existe un rol con el ID dado.
     *
     * @param roleId ID del rol
     * @return true si existe
     */
    public boolean existsById(Integer roleId) {
        return RoleType.fromId(roleId).isPresent();
    }

    /**
     * Verifica si existe un rol con el nombre dado.
     *
     * @param roleName Nombre del rol
     * @return true si existe
     */
    public boolean existsByName(String roleName) {
        return RoleType.fromName(roleName).isPresent();
    }

    /**
     * Obtiene el rol por defecto para nuevos usuarios.
     *
     * @return Rol CLIENT
     */
    public EmbeddedRole getDefaultRole() {
        return RoleType.getDefault().toEmbeddedRole();
    }
}
