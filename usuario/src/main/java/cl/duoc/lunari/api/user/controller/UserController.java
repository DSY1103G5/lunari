package cl.duoc.lunari.api.user.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.model.UserRole;
import cl.duoc.lunari.api.user.service.UserService;
import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.payload.ResponseUtil;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Devuelve todos los usuarios con paginación y ordenamiento mejorado.
     * 
     * @param page número de página (0-indexed, default: 0)
     * @param size tamaño de página (default: 10)
     * @param sort campo y dirección de ordenamiento (ej: firstName,asc o createdAt,desc)
     * @param active filtro por estado activo (opcional)
     * @param roleId filtro por rol (opcional)
     * @param companyId filtro por empresa (opcional)
     * @return Página de usuarios (HTTP 200)
     */
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) UUID companyId) {
        try {
            // Parse sort parameter
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            Page<User> users = userService.getUsersPaginated(pageable, active, roleId, companyId);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Devuelve todos los usuarios (versión simple).
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

    /**
     * Buscar usuarios por nombre o apellido.
     * 
     * @param query término de búsqueda
     * @param page número de página
     * @param size tamaño de página
     * @return usuarios que coinciden con la búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<User>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("El término de búsqueda no puede estar vacío", 
                                HttpStatus.BAD_REQUEST.value()));
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
            Page<User> users = userService.searchUsers(query.trim(), pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar usuarios: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtener usuarios por empresa.
     * 
     * @param companyId ID de la empresa
     * @param page número de página
     * @param size tamaño de página
     * @return usuarios de la empresa especificada
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersByCompany(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
            Page<User> users = userService.getUsersByCompanyPaginated(companyId, pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios de la empresa: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtener usuarios por rol.
     * 
     * @param roleId ID del rol
     * @param page número de página
     * @param size tamaño de página
     * @return usuarios con el rol especificado
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersByRole(
            @PathVariable Integer roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
            Page<User> users = userService.getUsersByRolePaginated(roleId, pageable);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios por rol: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtener estadísticas de usuarios.
     * 
     * @return estadísticas básicas de usuarios
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getUserStats() {
        try {
            Object stats = userService.getUserStats();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener estadísticas: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Activar/desactivar usuario.
     * 
     * @param id ID del usuario
     * @param active estado activo
     * @return usuario actualizado
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(@PathVariable UUID id, @RequestParam Boolean active) {
        try {
            User updatedUser = userService.updateUserStatus(id, active);
            return ResponseEntity.ok(ApiResponse.success(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Cambiar contraseña del usuario.
     * 
     * @param id ID del usuario
     * @param newPassword nueva contraseña
     * @return confirmación del cambio
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@PathVariable UUID id, @RequestBody String newPassword) {
        try {
            userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Asignar rol a usuario.
     * 
     * @param userId ID del usuario
     * @param roleId ID del rol
     * @return confirmación de la asignación
     */
    @PatchMapping("/{userId}/role/{roleId}")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(@PathVariable UUID userId, @PathVariable Integer roleId) {
        try {
            userService.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario o rol no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Asignar usuario a empresa.
     * 
     * @param userId ID del usuario
     * @param companyId ID de la empresa
     * @return confirmación de la asignación
     */
    @PatchMapping("/{userId}/company/{companyId}")
    public ResponseEntity<ApiResponse<Void>> assignUserToCompany(@PathVariable UUID userId, @PathVariable UUID companyId) {
        try {
            userService.assignUserToCompany(userId, companyId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario o empresa no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Verificar usuario con token.
     * 
     * @param token token de verificación
     * @return confirmación de la verificación
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@RequestParam String token) {
        try {
            userService.verifyUser(token);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Token inválido o expirado", HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtener todos los roles disponibles.
     * 
     * @return lista de roles
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<UserRole>>> getAllRoles() {
        try {
            List<UserRole> roles = userService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener roles: " + e.getMessage(), 
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}