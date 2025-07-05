package cl.duoc.lunari.api.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.model.UserRole;

public interface UserService {
    // Funcionalidades básicas
    User createUser(User user);
    List<User> getAllUsers(Pageable pageable);
    Optional<User> getUserById(UUID id);
    Optional<User> getUserByEmail(String email);
    User updateUser(UUID id, User userDetails);
    void deleteUser(UUID id);

    // Autenticación
    void verifyUser(String token);
    void updatePassword(UUID id, String newPassword);

    // Gestión de roles
    void assignRoleToUser(UUID userId, Integer roleId);
    List<UserRole> getAllRoles();

    // Gestión de empresas
    List<User> getUsersByCompany(UUID companyId);
    void assignUserToCompany(UUID userId, UUID companyId);

    // Paginación y búsqueda
    Page<User> getUsersPaginated(Pageable pageable, Boolean active, Integer roleId, UUID companyId);
    Page<User> searchUsers(String query, Pageable pageable);
    Page<User> getUsersByCompanyPaginated(UUID companyId, Pageable pageable);
    Page<User> getUsersByRolePaginated(Integer roleId, Pageable pageable);
    
    // Estadísticas
    Object getUserStats();
    
    // Gestión de estado
    User updateUserStatus(UUID id, Boolean active);
}