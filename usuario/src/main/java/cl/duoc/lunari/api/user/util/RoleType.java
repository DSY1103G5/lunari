package cl.duoc.lunari.api.user.util;

import cl.duoc.lunari.api.user.model.EmbeddedRole;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum de roles predefinidos en el sistema LUNARi.
 *
 * Define los roles disponibles y sus propiedades.
 * Este enum reemplaza la tabla Rol de PostgreSQL,
 * embebiendo la información del rol directamente en cada usuario.
 */
public enum RoleType {

    ADMIN(1, "ADMIN", "Administrador con acceso completo al sistema"),
    PRODUCT_OWNER(2, "PRODUCT_OWNER", "Product Owner que gestiona proyectos y requerimientos"),
    CLIENT(3, "CLIENT", "Cliente con acceso limitado a su portal"),
    DEVOPS(4, "DEVOPS", "Ingeniero DevOps que gestiona despliegues y monitoreo");

    private final Integer id;
    private final String name;
    private final String description;

    RoleType(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convierte este enum a un objeto EmbeddedRole.
     *
     * @return EmbeddedRole con los datos de este rol
     */
    public EmbeddedRole toEmbeddedRole() {
        EmbeddedRole role = new EmbeddedRole();
        role.setRoleId(this.id);
        role.setRoleName(this.name);
        role.setRoleDescription(this.description);
        return role;
    }

    /**
     * Busca un RoleType por su ID.
     *
     * @param id ID del rol
     * @return Optional conteniendo el RoleType si se encuentra
     */
    public static Optional<RoleType> fromId(Integer id) {
        return Arrays.stream(values())
            .filter(role -> role.getId().equals(id))
            .findFirst();
    }

    /**
     * Busca un RoleType por su nombre.
     *
     * @param name Nombre del rol (case-insensitive)
     * @return Optional conteniendo el RoleType si se encuentra
     */
    public static Optional<RoleType> fromName(String name) {
        return Arrays.stream(values())
            .filter(role -> role.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    /**
     * Obtiene el rol por defecto para nuevos usuarios.
     *
     * @return RoleType CLIENT como rol por defecto
     */
    public static RoleType getDefault() {
        return CLIENT;
    }
}
