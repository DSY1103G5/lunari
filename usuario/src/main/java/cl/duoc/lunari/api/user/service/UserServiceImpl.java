package cl.duoc.lunari.api.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.model.UserRole;
import cl.duoc.lunari.api.user.repository.RoleRepository;
import cl.duoc.lunari.api.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email ya existe: " + user.getEmail());
        }

        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).getContent();
    }

    @Override
    public User updateUser(UUID id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Check si el email existe en la base de datos
        if (!user.getEmail().equals(userDetails.getEmail())) {
            Optional<User> existingUserWithEmail = userRepository.findByEmail(userDetails.getEmail());
            if (existingUserWithEmail.isPresent()) {
                User existingUser = existingUserWithEmail.get();
                if (!existingUser.getId().equals(id)) {
                    throw new RuntimeException("Email ya existe en la base de datos: " + userDetails.getEmail());
                }
            }
        }

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setProfileImage(userDetails.getProfileImage());
        user.setRoleId(userDetails.getRoleId());
        user.setCompanyId(userDetails.getCompanyId());
        user.setActive(userDetails.getActive());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword()); // Placeholder: In real app, hash it!
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void verifyUser(String token) {
        // This method would typically:
        // 1. Validate the verification token
        // 2. Find the user associated with this token
        // 3. Update the user's verification status

        // For implementation, you would need a token repository and logic
        throw new UnsupportedOperationException("Method verifyUser not yet implemented");
    }

    @Override
    @Transactional
    public void updatePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        user.setRoleId(roleId);
        userRepository.save(user);
    }

    @Override
    public List<UserRole> getAllRoles() {
        return roleRepository.findAll(); 
    }

    @Override
    public List<User> getUsersByCompany(UUID companyId) {
        return userRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void assignUserToCompany(UUID userId, UUID companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        user.setCompanyId(companyId);
        userRepository.save(user);
    }
}