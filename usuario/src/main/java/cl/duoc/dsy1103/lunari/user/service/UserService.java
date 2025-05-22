package cl.duoc.dsy1103.lunari.user.service;

import java.util.List;
import java.util.Optional;

import cl.duoc.dsy1103.lunari.user.model.User;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
}
