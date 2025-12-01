package cl.duoc.lunari.api.user.repository;

import cl.duoc.lunari.api.user.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de usuarios para DynamoDB.
 *
 * Reemplaza JpaRepository con operaciones personalizadas usando
 * DynamoDB Enhanced Client.
 *
 * Diferencias clave con JPA:
 * - Usa String userId en lugar de UUID
 * - Usa roleName en lugar de roleId
 * - Paginación con tokens en lugar de Pageable
 * - Retorna DynamoDbPage en lugar de Page
 * - No hay métodos de company (removido)
 */
public interface UserRepository {

    // ==================== Operaciones CRUD Básicas ====================

    /**
     * Guarda o actualiza un usuario en DynamoDB.
     *
     * @param user Usuario a guardar
     * @return Usuario guardado
     */
    User save(User user);

    /**
     * Busca un usuario por su ID.
     *
     * @param userId ID del usuario (UUID como String)
     * @return Optional con el usuario si existe
     */
    Optional<User> findById(String userId);

    /**
     * Busca un usuario por email (usando EmailIndex GSI).
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Elimina un usuario por su ID.
     *
     * @param userId ID del usuario
     */
    void deleteById(String userId);

    /**
     * Verifica si existe un usuario con el ID dado.
     *
     * @param userId ID del usuario
     * @return true si existe
     */
    boolean existsById(String userId);

    // ==================== Operaciones de Listado y Paginación ====================

    /**
     * Obtiene todos los usuarios con paginación.
     *
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token para continuar desde la última página (null para primera página)
     * @return Página de usuarios
     */
    DynamoDbPage<User> findAll(int limit, String paginationToken);

    /**
     * Busca usuarios por nombre de rol (usando RoleActiveIndex GSI).
     *
     * @param roleName Nombre del rol (ADMIN, CLIENT, etc.)
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios con ese rol
     */
    DynamoDbPage<User> findByRoleName(String roleName, int limit, String paginationToken);

    /**
     * Busca usuarios por nombre de rol y estado activo (usando RoleActiveIndex GSI).
     *
     * Usa el composite sort key "isActive#userId" para filtrado eficiente.
     *
     * @param roleName Nombre del rol
     * @param active Estado activo
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios filtrados
     */
    DynamoDbPage<User> findByRoleNameAndActive(String roleName, Boolean active, int limit, String paginationToken);

    /**
     * Busca usuarios por estado activo (scan con filtro).
     *
     * @param active Estado activo
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios
     */
    DynamoDbPage<User> findByActive(Boolean active, int limit, String paginationToken);

    /**
     * Busca usuarios con múltiples filtros opcionales (scan con filtros).
     *
     * @param active Estado activo (null para ignorar)
     * @param roleName Nombre del rol (null para ignorar)
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios filtrados
     */
    DynamoDbPage<User> findUsersWithFilters(Boolean active, String roleName, int limit, String paginationToken);

    /**
     * Busca usuarios por texto en firstName, lastName o email (scan con filtro).
     *
     * Búsqueda case-insensitive tipo "contains".
     *
     * @param query Texto a buscar
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios que coinciden
     */
    DynamoDbPage<User> searchUsers(String query, int limit, String paginationToken);

    // ==================== Operaciones de Conteo ====================

    /**
     * Cuenta el total de usuarios (scan completo).
     *
     * NOTA: Operación costosa en DynamoDB. Considerar cachear el resultado.
     *
     * @return Cantidad total de usuarios
     */
    long count();

    /**
     * Cuenta usuarios por estado activo (scan con filtro).
     *
     * @param active Estado activo
     * @return Cantidad de usuarios
     */
    long countByActive(Boolean active);

    /**
     * Cuenta usuarios por nombre de rol (query en RoleActiveIndex).
     *
     * @param roleName Nombre del rol
     * @return Cantidad de usuarios con ese rol
     */
    long countByRoleName(String roleName);
}
