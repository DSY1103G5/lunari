package cl.duoc.lunari.api.user.repository;

import cl.duoc.lunari.api.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de usuarios para PostgreSQL usando Spring Data JPA.
 *
 * Spring Data JPA proporciona automáticamente implementaciones para:
 * - save, findById, findAll, deleteById, existsById, count
 *
 * Métodos adicionales con query derivadas:
 * - findByEmail, findByUsername
 * - findByIsActive
 * - countByIsActive
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ==================== Operaciones CRUD Básicas (proporcionadas por JpaRepository) ====================
    // save(User user)
    // Optional<User> findById(String id)
    // void deleteById(String id)
    // boolean existsById(String id)
    // List<User> findAll()
    // Page<User> findAll(Pageable pageable)
    // long count()

    // ==================== Búsquedas Personalizadas ====================

    /**
     * Busca un usuario por email.
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por username.
     *
     * @param username Username del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca usuarios por estado activo con paginación.
     *
     * @param isActive Estado activo
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Busca usuarios por estado verificado con paginación.
     *
     * @param isVerified Estado verificado
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    Page<User> findByIsVerified(Boolean isVerified, Pageable pageable);

    /**
     * Busca usuarios por estado activo y verificado.
     *
     * @param isActive Estado activo
     * @param isVerified Estado verificado
     * @param pageable Configuración de paginación
     * @return Página de usuarios
     */
    Page<User> findByIsActiveAndIsVerified(Boolean isActive, Boolean isVerified, Pageable pageable);

    /**
     * Busca usuarios por texto en username, email o datos personales.
     *
     * Búsqueda case-insensitive en múltiples campos usando JSONB.
     *
     * @param query Texto a buscar
     * @param pageable Configuración de paginación
     * @return Página de usuarios que coinciden
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    // ==================== Operaciones de Conteo ====================

    /**
     * Cuenta usuarios por estado activo.
     *
     * @param isActive Estado activo
     * @return Cantidad de usuarios
     */
    long countByIsActive(Boolean isActive);

    /**
     * Cuenta usuarios por estado verificado.
     *
     * @param isVerified Estado verificado
     * @return Cantidad de usuarios
     */
    long countByIsVerified(Boolean isVerified);

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email Email del usuario
     * @return true si existe
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el username dado.
     *
     * @param username Username del usuario
     * @return true si existe
     */
    boolean existsByUsername(String username);
}
