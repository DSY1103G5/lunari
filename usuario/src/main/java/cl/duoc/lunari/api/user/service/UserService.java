package cl.duoc.lunari.api.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.model.UserRole;

public interface UserService {
    // CORE FUNCTIONALITY
    User createUser(User user);
    List<User> getAllUsers(Pageable pageable);
    Optional<User> getUserById(UUID id);
    Optional<User> getUserByEmail(String email);
    User updateUser(UUID id, User userDetails);
    void deleteUser(UUID id);

    // AUTH FUNCTIONALITY
    void verifyUser(String token);
    void updatePassword(UUID id, String newPassword);

    // ROLE MANAGEMENT FUNCTIONALITY
    void assignRoleToUser(UUID userId, Integer roleId);
    List<UserRole> getAllRoles();

    // COMPANY MANAGEMENT FUNCTIONALITY
    List<User> getUsersByCompany(UUID companyId);
    void assignUserToCompany(UUID userId, UUID companyId);

    // PAGINATION AND SEARCH FUNCTIONALITY
    Page<User> getUsersPaginated(Pageable pageable, Boolean active, Integer roleId, UUID companyId);
    Page<User> searchUsers(String query, Pageable pageable);
    Page<User> getUsersByCompanyPaginated(UUID companyId, Pageable pageable);
    Page<User> getUsersByRolePaginated(Integer roleId, Pageable pageable);
    
    // STATISTICS FUNCTIONALITY
    Object getUserStats();
    
    // STATUS MANAGEMENT FUNCTIONALITY
    User updateUserStatus(UUID id, Boolean active);
}