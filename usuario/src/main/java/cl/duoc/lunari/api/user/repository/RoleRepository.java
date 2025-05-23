package cl.duoc.lunari.api.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.lunari.api.user.model.UserRole;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Long> {
	
}
