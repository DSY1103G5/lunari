package cl.duoc.lunari.api.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.duoc.lunari.api.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    List<User> findByCompanyId(UUID companyId);
    Page<User> findByCompanyId(UUID companyId, Pageable pageable);
    Page<User> findByRoleId(Integer roleId, Pageable pageable);
    Page<User> findByActive(Boolean active, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:active IS NULL OR u.active = :active) AND " +
           "(:roleId IS NULL OR u.roleId = :roleId) AND " +
           "(:companyId IS NULL OR u.companyId = :companyId)")
    Page<User> findUsersWithFilters(@Param("active") Boolean active, 
                                   @Param("roleId") Integer roleId, 
                                   @Param("companyId") UUID companyId, 
                                   Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
    
    long countByActive(Boolean active);
    long countByRoleId(Integer roleId);
    long countByCompanyId(UUID companyId);
}