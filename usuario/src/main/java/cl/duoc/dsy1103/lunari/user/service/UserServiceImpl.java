package cl.duoc.dsy1103.lunari.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import cl.duoc.dsy1103.lunari.user.model.User;

import cl.duoc.dsy1103.lunari.user.repository.UserRepository;

public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // Uncomment and inject if
    // using Spring Security

    @Autowired
    public UserServiceImpl(UserRepository userRepository) { // , PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        // this.passwordEncoder = passwordEncoder; // Uncomment
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
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User userDetails) {
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

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword()); // Placeholder: In real app, hash it!
        }
        
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
