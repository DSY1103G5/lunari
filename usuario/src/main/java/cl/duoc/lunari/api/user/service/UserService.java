package cl.duoc.lunari.api.user.service;

import cl.duoc.lunari.api.user.model.*;
import cl.duoc.lunari.api.user.repository.DynamoDbPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de usuarios para DynamoDB.
 *
 * Cambios desde JPA:
 * - UUID → String para userId
 * - Pageable → limit/paginationToken
 * - Page<User> → DynamoDbPage<User>
 * - Removidos métodos de company
 * - Agregados métodos de e-commerce y gamificación
 */
public interface UserService {

    // ==================== Funcionalidades Básicas ====================

    /**
     * Crea un nuevo usuario.
     *
     * @param user Usuario a crear (sin userId, se generará automáticamente)
     * @return Usuario creado con userId asignado
     */
    User createUser(User user);

    /**
     * Obtiene todos los usuarios con paginación.
     *
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación (null para primera página)
     * @return Página de usuarios
     */
    DynamoDbPage<User> getAllUsers(int limit, String paginationToken);

    /**
     * Obtiene un usuario por su ID.
     *
     * @param userId ID del usuario (UUID como String)
     * @return Optional con el usuario si existe
     */
    Optional<User> getUserById(String userId);

    /**
     * Obtiene un usuario por email.
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Actualiza un usuario existente.
     *
     * @param userId ID del usuario
     * @param userDetails Detalles del usuario a actualizar
     * @return Usuario actualizado
     */
    User updateUser(String userId, User userDetails);

    /**
     * Elimina un usuario.
     *
     * @param userId ID del usuario
     */
    void deleteUser(String userId);

    // ==================== Autenticación y Verificación ====================

    /**
     * Autentica un usuario usando email/username y contraseña.
     *
     * @param identifier Email o username del usuario
     * @param password Contraseña del usuario
     * @return Usuario autenticado
     * @throws RuntimeException si las credenciales son inválidas o el usuario no existe
     */
    User authenticateUser(String identifier, String password);

    /**
     * Verifica un usuario usando su token de verificación.
     *
     * @param token Token de verificación
     */
    void verifyUser(String token);

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param userId ID del usuario
     * @param newPassword Nueva contraseña (se debe hashear antes de llamar)
     */
    void updatePassword(String userId, String newPassword);

    // ==================== Gestión de Roles ====================

    /**
     * Asigna un rol a un usuario.
     *
     * @param userId ID del usuario
     * @param roleId ID del rol (1=ADMIN, 2=PRODUCT_OWNER, 3=CLIENT, 4=DEVOPS)
     */
    void assignRoleToUser(String userId, Integer roleId);

    /**
     * Asigna un rol a un usuario por nombre.
     *
     * @param userId ID del usuario
     * @param roleName Nombre del rol (ADMIN, CLIENT, etc.)
     */
    void assignRoleToUserByName(String userId, String roleName);

    // ==================== Paginación y Búsqueda ====================

    /**
     * Obtiene usuarios con filtros opcionales.
     *
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @param active Estado activo (null para ignorar)
     * @param roleName Nombre del rol (null para ignorar)
     * @return Página de usuarios filtrados
     */
    DynamoDbPage<User> getUsersFiltered(int limit, String paginationToken, Boolean active, String roleName);

    /**
     * Busca usuarios por texto en nombre o email.
     *
     * @param query Texto a buscar
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios que coinciden
     */
    DynamoDbPage<User> searchUsers(String query, int limit, String paginationToken);

    /**
     * Obtiene usuarios por rol con paginación.
     *
     * @param roleName Nombre del rol
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios con ese rol
     */
    DynamoDbPage<User> getUsersByRole(String roleName, int limit, String paginationToken);

    /**
     * Obtiene usuarios por rol y estado activo.
     *
     * @param roleName Nombre del rol
     * @param active Estado activo
     * @param limit Cantidad máxima de resultados
     * @param paginationToken Token de paginación
     * @return Página de usuarios filtrados
     */
    DynamoDbPage<User> getUsersByRoleAndActive(String roleName, Boolean active, int limit, String paginationToken);

    // ==================== Estadísticas ====================

    /**
     * Obtiene estadísticas generales de usuarios.
     *
     * @return Objeto con estadísticas
     */
    Object getUserStats();

    // ==================== Gestión de Estado ====================

    /**
     * Actualiza el estado activo de un usuario.
     *
     * @param userId ID del usuario
     * @param active Nuevo estado
     * @return Usuario actualizado
     */
    User updateUserStatus(String userId, Boolean active);

    // ==================== Gamificación ====================

    /**
     * Agrega puntos a un usuario.
     *
     * @param userId ID del usuario
     * @param points Puntos a agregar (puede ser negativo para restar)
     * @return Usuario actualizado
     */
    User addPoints(String userId, Long points);

    /**
     * Actualiza el nivel de un usuario.
     *
     * @param userId ID del usuario
     * @param newLevel Nuevo nivel
     * @return Usuario actualizado
     */
    User updateLevel(String userId, Integer newLevel);

    // ==================== Favoritos ====================

    /**
     * Agrega un servicio a favoritos del usuario.
     *
     * @param userId ID del usuario
     * @param serviceId ID del servicio
     * @return Usuario actualizado
     */
    User addFavorite(String userId, Integer serviceId);

    /**
     * Remueve un servicio de favoritos del usuario.
     *
     * @param userId ID del usuario
     * @param serviceId ID del servicio
     * @return Usuario actualizado
     */
    User removeFavorite(String userId, Integer serviceId);

    /**
     * Obtiene la lista de servicios favoritos de un usuario.
     *
     * @param userId ID del usuario
     * @return Lista de IDs de servicios favoritos
     */
    List<Integer> getFavorites(String userId);

    // ==================== Tracking de Compras ====================

    /**
     * Registra una compra realizada por el usuario.
     *
     * @param userId ID del usuario
     * @param amount Monto de la compra
     * @return Usuario actualizado
     */
    User recordPurchase(String userId, BigDecimal amount);

    // ==================== Tracking de Reviews ====================

    /**
     * Registra una review hecha por el usuario.
     *
     * @param userId ID del usuario
     * @param rating Calificación otorgada (1-5)
     * @return Usuario actualizado
     */
    User recordReview(String userId, Double rating);

    // ==================== Preferencias ====================

    /**
     * Actualiza las preferencias del usuario.
     *
     * @param userId ID del usuario
     * @param preferences Nuevas preferencias
     * @return Usuario actualizado
     */
    User updatePreferences(String userId, UserPreferences preferences);

    /**
     * Actualiza la dirección del usuario.
     *
     * @param userId ID del usuario
     * @param address Nueva dirección
     * @return Usuario actualizado
     */
    User updateAddress(String userId, Address address);

    /**
     * Actualiza los metadatos del usuario.
     *
     * @param userId ID del usuario
     * @param metadata Nuevos metadatos
     * @return Usuario actualizado
     */
    User updateMetadata(String userId, Metadata metadata);
}
