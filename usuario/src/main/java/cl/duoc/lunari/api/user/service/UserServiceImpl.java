package cl.duoc.lunari.api.user.service;

import cl.duoc.lunari.api.user.model.*;
import cl.duoc.lunari.api.user.repository.DynamoDbPage;
import cl.duoc.lunari.api.user.repository.UserRepository;
import cl.duoc.lunari.api.user.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;

/**
 * Implementación del servicio de clientes para e-commerce.
 *
 * Cambios principales:
 * - Usa estructura anidada (personal, address, preferences, gaming, stats, coupons)
 * - Enfocado en clientes de e-commerce, no en administración de usuarios
 * - Removido: roles, metadata, token verification (van a auth service)
 * - Añadido: gestión de cupones, perfil gaming, preferencias de cliente
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==================== Funcionalidades Básicas ====================

    @Override
    public User createUser(User user) {
        // Validar email único
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya existe: " + user.getEmail());
        }

        // Generar ID (puede ser numérico secuencial o UUID)
        user.setId(UUID.randomUUID().toString());

        // Establecer timestamps
        String now = DateTimeUtil.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Inicializar información personal si no existe
        if (user.getPersonal() == null) {
            user.setPersonal(new Personal());
        }
        // Establecer memberSince en personal
        if (user.getPersonal().getMemberSince() == null) {
            user.getPersonal().setMemberSince(String.valueOf(Year.now().getValue()));
        }

        // Inicializar estadísticas del cliente
        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        // Inicializar preferencias por defecto
        if (user.getPreferences() == null) {
            user.setPreferences(ClientPreferences.createDefault());
        }

        // Inicializar lista de cupones vacía
        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }

        // Estado inicial: activo pero no verificado
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getIsVerified() == null) {
            user.setIsVerified(false);
        }

        log.info("Creating new client with email: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public DynamoDbPage<User> getAllUsers(int limit, String paginationToken) {
        return userRepository.findAll(limit, paginationToken);
    }

    @Override
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateUser(String userId, User userDetails) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        // Validar email único (si cambió)
        if (!user.getEmail().equals(userDetails.getEmail())) {
            Optional<User> existingUserWithEmail = userRepository.findByEmail(userDetails.getEmail());
            if (existingUserWithEmail.isPresent()) {
                User existingUser = existingUserWithEmail.get();
                if (!existingUser.getId().equals(userId)) {
                    throw new RuntimeException("Email ya existe: " + userDetails.getEmail());
                }
            }
        }

        // Actualizar email y username
        user.setEmail(userDetails.getEmail());
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }

        // Actualizar información personal (nested object)
        if (userDetails.getPersonal() != null) {
            user.setPersonal(userDetails.getPersonal());
        }

        // Actualizar dirección
        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }

        // Actualizar preferencias
        if (userDetails.getPreferences() != null) {
            user.setPreferences(userDetails.getPreferences());
        }

        // Actualizar perfil gaming
        if (userDetails.getGaming() != null) {
            user.setGaming(userDetails.getGaming());
        }

        // Actualizar estado
        if (userDetails.getIsActive() != null) {
            user.setIsActive(userDetails.getIsActive());
        }

        // Actualizar contraseña solo si se proporciona
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }

        // Actualizar timestamp
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updating client: {}", userId);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + userId);
        }
        log.info("Deleting client: {}", userId);
        userRepository.deleteById(userId);
    }

    // ==================== Autenticación y Verificación ====================

    @Override
    public User authenticateUser(String identifier, String password) {
        log.info("Attempting authentication for identifier: {}", identifier);

        // Intentar buscar por email primero (más común)
        Optional<User> userOptional = userRepository.findByEmail(identifier);

        // Si no se encuentra por email, intentar por username
        // NOTA: Esto requiere implementar findByUsername en el repositorio
        // Por ahora, solo soportamos autenticación por email
        if (userOptional.isEmpty()) {
            log.warn("User not found with identifier: {}", identifier);
            throw new RuntimeException("Credenciales inválidas");
        }

        User user = userOptional.get();

        // Verificar contraseña
        // NOTA: En producción, se debe usar BCrypt o similar para hashear contraseñas
        if (!password.equals(user.getPassword())) {
            log.warn("Invalid password for user: {}", identifier);
            throw new RuntimeException("Credenciales inválidas");
        }

        // Verificar que el usuario esté activo
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Inactive user attempted login: {}", identifier);
            throw new RuntimeException("Usuario inactivo. Contacte al administrador.");
        }

        log.info("Successful authentication for user: {} (ID: {})", identifier, user.getId());
        return user;
    }

    @Override
    public void verifyUser(String token) {
        // TODO: Este método debería moverse a un servicio de autenticación separado
        throw new UnsupportedOperationException("Verificación movida al servicio de autenticación");
    }

    @Override
    public void updatePassword(String userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        user.setPassword(newPassword);
        user.setUpdatedAt(DateTimeUtil.now());

        userRepository.save(user);
        log.info("Password updated for client: {}", userId);
    }

    // ==================== Gestión de Roles ====================
    // Nota: Clientes no tienen roles - removido

    @Override
    public void assignRoleToUser(String userId, Integer roleId) {
        throw new UnsupportedOperationException("Los clientes no tienen roles");
    }

    @Override
    public void assignRoleToUserByName(String userId, String roleName) {
        throw new UnsupportedOperationException("Los clientes no tienen roles");
    }

    // ==================== Paginación y Búsqueda ====================

    @Override
    public DynamoDbPage<User> getUsersFiltered(int limit, String paginationToken, Boolean active, String roleName) {
        // roleName ignorado para clientes
        return userRepository.findUsersWithFilters(active, null, limit, paginationToken);
    }

    @Override
    public DynamoDbPage<User> searchUsers(String query, int limit, String paginationToken) {
        return userRepository.searchUsers(query, limit, paginationToken);
    }

    @Override
    public DynamoDbPage<User> getUsersByRole(String roleName, int limit, String paginationToken) {
        throw new UnsupportedOperationException("Los clientes no tienen roles");
    }

    @Override
    public DynamoDbPage<User> getUsersByRoleAndActive(String roleName, Boolean active, int limit, String paginationToken) {
        throw new UnsupportedOperationException("Los clientes no tienen roles");
    }

    // ==================== Estadísticas ====================

    @Override
    public Object getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalClients", userRepository.count());
        stats.put("activeClients", userRepository.countByActive(true));
        stats.put("inactiveClients", userRepository.countByActive(false));

        return stats;
    }

    // ==================== Gestión de Estado ====================

    @Override
    public User updateUserStatus(String userId, Boolean active) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        user.setIsActive(active);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updated status for client {}: {}", userId, active);
        return userRepository.save(user);
    }

    // ==================== Gamificación y Puntos ====================

    @Override
    public User addPoints(String userId, Long points) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        user.getStats().addPoints(points);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Added {} points to client {}: Total: {}", points, userId, user.getStats().getPoints());
        return userRepository.save(user);
    }

    @Override
    public User updateLevel(String userId, Integer newLevel) {
        // Para e-commerce, el nivel es String (Bronze, Silver, Gold, etc.)
        throw new UnsupportedOperationException("Use updateClientLevel(userId, levelName) para clientes");
    }

    /**
     * Actualiza el nivel del cliente (Bronze, Silver, Gold, Platinum, etc.).
     *
     * @param userId ID del cliente
     * @param levelName Nombre del nivel
     * @return Cliente actualizado
     */
    public User updateClientLevel(String userId, String levelName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        user.getStats().setLevel(levelName);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updated level for client {}: {}", userId, levelName);
        return userRepository.save(user);
    }

    // ==================== Favoritos ====================
    // Nota: Favoritos ahora se trackean en stats.favorites (contador)

    @Override
    public User addFavorite(String userId, Integer serviceId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        user.getStats().incrementFavorites();
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Added favorite to client {}: Total favorites: {}", userId, user.getStats().getFavorites());
        return userRepository.save(user);
    }

    @Override
    public User removeFavorite(String userId, Integer serviceId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getStats() == null || user.getStats().getFavorites() == 0) {
            throw new RuntimeException("No hay favoritos para remover");
        }

        // Decrementar contador (asegurarnos que no sea negativo)
        int currentFavorites = user.getStats().getFavorites();
        user.getStats().setFavorites(Math.max(0, currentFavorites - 1));
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Removed favorite from client {}: Total favorites: {}", userId, user.getStats().getFavorites());
        return userRepository.save(user);
    }

    @Override
    public List<Integer> getFavorites(String userId) {
        // Para e-commerce, los favoritos son tracked en otra tabla/servicio
        // Aquí solo tenemos el contador
        throw new UnsupportedOperationException("Use el servicio de favoritos para obtener la lista completa");
    }

    // ==================== Tracking de Compras ====================

    @Override
    public User recordPurchase(String userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        user.getStats().incrementPurchases();
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Recorded purchase for client {}: Total purchases: {}", userId, user.getStats().getPurchases());
        return userRepository.save(user);
    }

    // ==================== Tracking de Reviews ====================

    @Override
    public User recordReview(String userId, Double rating) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getStats() == null) {
            user.setStats(ClientStats.createDefault());
        }

        user.getStats().incrementReviews();
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Recorded review for client {}: Total reviews: {}", userId, user.getStats().getReviews());
        return userRepository.save(user);
    }

    // ==================== Preferencias ====================

    @Override
    public User updatePreferences(String userId, UserPreferences preferences) {
        // UserPreferences ya no existe - usar ClientPreferences
        throw new UnsupportedOperationException("Use updateClientPreferences(userId, ClientPreferences)");
    }

    /**
     * Actualiza las preferencias del cliente.
     *
     * @param userId ID del cliente
     * @param preferences Nuevas preferencias
     * @return Cliente actualizado
     */
    public User updateClientPreferences(String userId, ClientPreferences preferences) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        user.setPreferences(preferences);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updated preferences for client: {}", userId);
        return userRepository.save(user);
    }

    @Override
    public User updateAddress(String userId, Address address) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        user.setAddress(address);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updated address for client: {}", userId);
        return userRepository.save(user);
    }

    @Override
    public User updateMetadata(String userId, Metadata metadata) {
        // Metadata removido para clientes de e-commerce
        throw new UnsupportedOperationException("Metadata removido para clientes");
    }

    // ==================== Gestión de Cupones ====================

    /**
     * Obtiene todos los cupones del cliente.
     *
     * @param userId ID del cliente
     * @return Lista de cupones
     */
    public List<Coupon> getClientCoupons(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        return user.getCoupons() != null ? user.getCoupons() : new ArrayList<>();
    }

    /**
     * Añade un cupón al cliente.
     *
     * @param userId ID del cliente
     * @param coupon Cupón a añadir
     * @return Cliente actualizado
     */
    public User addCoupon(String userId, Coupon coupon) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getCoupons() == null) {
            user.setCoupons(new ArrayList<>());
        }

        user.getCoupons().add(coupon);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Added coupon {} to client {}", coupon.getCode(), userId);
        return userRepository.save(user);
    }

    /**
     * Marca un cupón como usado.
     *
     * @param userId ID del cliente
     * @param couponId ID del cupón
     * @return Cliente actualizado
     */
    public User useCoupon(String userId, String couponId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        if (user.getCoupons() == null) {
            throw new RuntimeException("Cliente no tiene cupones");
        }

        Coupon coupon = user.getCoupons().stream()
            .filter(c -> couponId.equals(c.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cupón no encontrado: " + couponId));

        if (Boolean.TRUE.equals(coupon.getIsUsed())) {
            throw new RuntimeException("Cupón ya fue usado");
        }

        coupon.markAsUsed();
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Marked coupon {} as used for client {}", couponId, userId);
        return userRepository.save(user);
    }

    // ==================== Perfil Gaming ====================

    /**
     * Actualiza el perfil gaming del cliente.
     *
     * @param userId ID del cliente
     * @param gaming Información gaming
     * @return Cliente actualizado
     */
    public User updateGamingProfile(String userId, Gaming gaming) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + userId));

        user.setGaming(gaming);
        user.setUpdatedAt(DateTimeUtil.now());

        log.info("Updated gaming profile for client: {}", userId);
        return userRepository.save(user);
    }
}
