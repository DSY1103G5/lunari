package cl.duoc.lunari.api.user.controller;

import java.util.Optional;
import java.util.UUID;

import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.service.UserService;
import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.payload.ResponseUtil;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Devuelve todos los usuarios.
     * 
     * @return Lista de usuarios (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(Pageable pageable) {
        try {
            List<User> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Devuelve un usuario específico por su ID.
     * 
     * @param id ID del usuario
     * @return El usuario si se encuentra (HTTP 200), o HTTP 404 si no se encuentra.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable UUID id) {
        Optional<User> userOptional = userService.getUserById(id);
        return ResponseUtil.fromOptional(userOptional, "Usuario no encontrado");
    }

    /**
     * Devuelve un usuario específico por su email.
     * 
     * @param email El email del usuario
     * @return El usuario si se encuentra (HTTP 200), o HTTP 404 si no se encuentra.
     */
    @GetMapping("/email")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@RequestParam String email) {
        // Sanitize email parameter
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email no puede ser nulo o vacío", HttpStatus.BAD_REQUEST.value()));
        }

        email = email.trim().toLowerCase();
        if (!email.matches("^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Formato de email inválido", HttpStatus.BAD_REQUEST.value()));
        }

        Optional<User> userOptional = userService.getUserByEmail(email);
        return ResponseUtil.fromOptional(userOptional, "Usuario no encontrado con ese email");
    }

    /**
     * Crea un usuario.
     * 
     * @param user user data
     * @return el usuario creado (HTTP 201)
     * @throws RuntimeException si el usuario ya existe (HTTP 409)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdUser));
        } catch (RuntimeException e) {
            // Check if it's a duplicate email error
            if (e.getMessage() != null && e.getMessage().contains("Email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage(), HttpStatus.CONFLICT.value()));
            }
            // Handle other database-related errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erro al registrar usuario: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza un usuario.
     * 
     * @param id          El ID del usuario a actualizar.
     * @param userDetails Los datos del usuario a actualizar.
     * @return El usuario actualizado (HTTP 200), o HTTP 404 si no se encuentra.
     * @throws RuntimeException si los datos proporcionados son inválidos o si no se
     *                          puede procesar (HTTP 400 o 422)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Elimina un usuario.
     * 
     * @param id El ID del usuario a eliminar.
     * @return HTTP 204 si se elimina correctamente, o HTTP 404 si no se encuentra.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null));
        } catch (RuntimeException e) { // Replace with specific exception
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }
}