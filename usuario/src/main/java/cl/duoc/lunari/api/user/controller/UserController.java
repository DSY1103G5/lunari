package cl.duoc.lunari.api.user.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.duoc.lunari.api.user.model.*;
import cl.duoc.lunari.api.user.repository.DynamoDbPage;
import cl.duoc.lunari.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.dto.PagedUserRepresentation;
import cl.duoc.lunari.api.user.dto.ApiRootDto;
import cl.duoc.lunari.api.user.dto.LoginRequest;
import cl.duoc.lunari.api.user.dto.LoginResponse;
import cl.duoc.lunari.api.user.assembler.UserModelAssembler;
import cl.duoc.lunari.api.user.assembler.PagedUserModelAssembler;
import cl.duoc.lunari.api.payload.ApiResponse;
import jakarta.validation.Valid;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Controlador REST para gestión de usuarios con DynamoDB.
 *
 * Cambios principales desde JPA:
 * - UUID → String para userId
 * - Pageable → limit/paginationToken
 * - Page<User> → DynamoDbPage<User>
 * - Removidos endpoints de company
 * - Agregados endpoints de gamificación y e-commerce
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuarios", description = "Usuarios de LUNARi")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;
    private final PagedUserModelAssembler pagedUserModelAssembler;

    public UserController(UserService userService,
            UserModelAssembler userModelAssembler,
            PagedUserModelAssembler pagedUserModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
        this.pagedUserModelAssembler = pagedUserModelAssembler;
    }

    // ==================== Funcionalidades Básicas ====================

    /**
     * Devuelve todos los usuarios con paginación basada en tokens.
     *
     * @param limit           cantidad máxima de resultados (default: 10)
     * @param paginationToken token para obtener la siguiente página (opcional)
     * @param active          filtro por estado activo (opcional)
     * @param roleName        filtro por rol (opcional)
     * @return Página de usuarios (HTTP 200)
     */
    @GetMapping("/paginated")
    @Operation(summary = "Obtener usuarios paginados", description = "Obtiene todos los usuarios con paginación basada en tokens y filtros")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> getUsersPaginated(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String paginationToken,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String roleName) {
        try {
            DynamoDbPage<User> users = userService.getUsersFiltered(limit, paginationToken, active, roleName);

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, active, roleName);

            return ResponseEntity.ok(ApiResponse.success(pagedRepresentation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Devuelve todos los usuarios (versión simple).
     *
     * @param limit           cantidad máxima de resultados (default: 10)
     * @param paginationToken token de paginación (opcional)
     * @return Lista de usuarios (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene todos los usuarios de LUNARi")
    public ResponseEntity<ApiResponse<CollectionModel<UserRepresentation>>> getAllUsers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String paginationToken) {

        try {
            DynamoDbPage<User> usersPage = userService.getAllUsers(limit, paginationToken);
            List<UserRepresentation> userRepresentations = usersPage.getItems().stream()
                    .map(userModelAssembler::toModel)
                    .toList();

            CollectionModel<UserRepresentation> collectionModel = CollectionModel.of(userRepresentations);
            collectionModel.add(linkTo(UserController.class).withSelfRel());
            collectionModel.add(linkTo(methodOn(UserController.class)
                    .getUsersPaginated(10, null, null, null))
                    .withRel("paginated"));
            collectionModel.add(linkTo(methodOn(UserController.class)
                    .getUserStats()).withRel("stats"));

            return ResponseEntity.ok(ApiResponse.success(collectionModel));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Devuelve un usuario específico por su ID.
     *
     * @param userId ID del usuario (UUID como String)
     * @return El usuario si se encuentra (HTTP 200), o HTTP 404 si no se encuentra.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario específico usando su ID único")
    public ResponseEntity<ApiResponse<UserRepresentation>> getUserById(@PathVariable String userId) {
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            UserRepresentation userRepresentation = userModelAssembler.toModel(userOptional.get());
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Devuelve un usuario específico por su email.
     *
     * @param email El email del usuario
     * @return El usuario si se encuentra (HTTP 200), o HTTP 404 si no se encuentra.
     */
    @GetMapping("/email")
    @Operation(summary = "Obtener usuario por email", description = "Obtiene un usuario específico usando su dirección de email")
    public ResponseEntity<ApiResponse<UserRepresentation>> getUserByEmail(@RequestParam String email) {
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
        if (userOptional.isPresent()) {
            UserRepresentation userRepresentation = userModelAssembler.toModel(userOptional.get());
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado con ese email", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Crea un usuario.
     *
     * @param user user data
     * @return el usuario creado (HTTP 201)
     * @throws RuntimeException si el usuario ya existe (HTTP 409)
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario en el sistema LUNARi")
    public ResponseEntity<ApiResponse<UserRepresentation>> registerUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            UserRepresentation userRepresentation = userModelAssembler.toModel(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            // Check if it's a duplicate email error
            if (e.getMessage() != null && e.getMessage().contains("Email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage(), HttpStatus.CONFLICT.value()));
            }
            // Handle other database-related errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al registrar usuario: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Autentica un usuario y retorna información de sesión.
     *
     * @param loginRequest Credenciales del usuario (email/username y password)
     * @return Información del usuario autenticado con token de sesión (HTTP 200)
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email/username y contraseña")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuario
            User user = userService.authenticateUser(
                loginRequest.getIdentifier(),
                loginRequest.getPassword()
            );

            // Generar un token de sesión simple (en producción, usar JWT)
            String sessionToken = UUID.randomUUID().toString();

            // Construir respuesta de login
            LoginResponse loginResponse = LoginResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role("CLIENT") // Por defecto todos son clientes en este sistema
                    .roleId(3) // CLIENT role ID
                    .isActive(user.getIsActive())
                    .isVerified(user.getIsVerified())
                    .sessionToken(sessionToken)
                    .level(user.getLevel())
                    .points(user.getPoints())
                    .message("¡Bienvenido de vuelta, " + user.getFullName() + "!")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(loginResponse));

        } catch (RuntimeException e) {
            // Error de autenticación
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        }
    }

    /**
     * Actualiza un usuario.
     *
     * @param userId      El ID del usuario a actualizar.
     * @param userDetails Los datos del usuario a actualizar.
     * @return El usuario actualizado (HTTP 200), o HTTP 404 si no se encuentra.
     * @throws RuntimeException si los datos proporcionados son inválidos o si no se
     *                          puede procesar (HTTP 400 o 422)
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateUser(@PathVariable String userId,
            @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(userId, userDetails);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Elimina un usuario.
     *
     * @param userId El ID del usuario a eliminar.
     * @return HTTP 204 si se elimina correctamente, o HTTP 404 si no se encuentra.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema usando su ID")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Búsqueda y Filtrado ====================

    /**
     * Buscar usuarios por nombre, apellido o email.
     *
     * @param query           término de búsqueda
     * @param limit           cantidad máxima de resultados (default: 10)
     * @param paginationToken token de paginación (opcional)
     * @return usuarios que coinciden con la búsqueda
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre, apellido o email con paginación")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String paginationToken) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("El término de búsqueda no puede estar vacío",
                                HttpStatus.BAD_REQUEST.value()));
            }

            DynamoDbPage<User> users = userService.searchUsers(query.trim(), limit, paginationToken);

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, null, null);

            return ResponseEntity.ok(ApiResponse.success(pagedRepresentation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar usuarios: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtener usuarios por rol.
     *
     * @param roleName        Nombre del rol (ADMIN, CLIENT, etc.)
     * @param limit           cantidad máxima de resultados (default: 10)
     * @param paginationToken token de paginación (opcional)
     * @return usuarios con el rol especificado
     */
    @GetMapping("/role/{roleName}")
    @Operation(summary = "Obtener usuarios por rol", description = "Obtiene todos los usuarios que tienen un rol específico")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> getUsersByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String paginationToken) {
        try {
            DynamoDbPage<User> users = userService.getUsersByRole(roleName, limit, paginationToken);

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, null, roleName);

            return ResponseEntity.ok(ApiResponse.success(pagedRepresentation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener usuarios por rol: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ==================== Estadísticas ====================

    /**
     * Obtener estadísticas de usuarios.
     *
     * @return estadísticas básicas de usuarios
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtener estadísticas de usuarios", description = "Obtiene estadísticas básicas y métricas de los usuarios del sistema")
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

    // ==================== Gestión de Estado ====================

    /**
     * Activar/desactivar usuario.
     *
     * @param userId ID del usuario
     * @param active estado activo
     * @return usuario actualizado
     */
    @PatchMapping("/{userId}/status")
    @Operation(summary = "Actualizar estado de usuario", description = "Activa o desactiva un usuario en el sistema")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateUserStatus(@PathVariable String userId,
            @RequestParam Boolean active) {
        try {
            User updatedUser = userService.updateUserStatus(userId, active);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Autenticación y Verificación ====================

    /**
     * Cambiar contraseña del usuario.
     *
     * @param userId      ID del usuario
     * @param newPassword nueva contraseña
     * @return confirmación del cambio
     */
    @PatchMapping("/{userId}/password")
    @Operation(summary = "Cambiar contraseña", description = "Actualiza la contraseña de un usuario específico")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@PathVariable String userId,
            @RequestBody String newPassword) {
        try {
            userService.updatePassword(userId, newPassword);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Verificar usuario con token.
     *
     * @param token token de verificación
     * @return confirmación de la verificación
     */
    @PostMapping("/verify")
    @Operation(summary = "Verificar usuario", description = "Verifica un usuario usando un token de verificación")
    public ResponseEntity<ApiResponse<Void>> verifyUser(@RequestParam String token) {
        try {
            userService.verifyUser(token);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Token inválido o expirado", HttpStatus.BAD_REQUEST.value()));
        }
    }

    // ==================== Gestión de Roles ====================

    /**
     * Asignar rol a usuario por ID.
     *
     * @param userId ID del usuario
     * @param roleId ID del rol (1=ADMIN, 2=PRODUCT_OWNER, 3=CLIENT, 4=DEVOPS)
     * @return confirmación de la asignación
     */
    @PatchMapping("/{userId}/role/id/{roleId}")
    @Operation(summary = "Asignar rol a usuario por ID", description = "Asigna un rol específico a un usuario usando el ID del rol")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUserById(@PathVariable String userId,
            @PathVariable Integer roleId) {
        try {
            userService.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario o rol no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Asignar rol a usuario por nombre.
     *
     * @param userId   ID del usuario
     * @param roleName Nombre del rol (ADMIN, CLIENT, etc.)
     * @return confirmación de la asignación
     */
    @PatchMapping("/{userId}/role/{roleName}")
    @Operation(summary = "Asignar rol a usuario por nombre", description = "Asigna un rol específico a un usuario usando el nombre del rol")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUserByName(@PathVariable String userId,
            @PathVariable String roleName) {
        try {
            userService.assignRoleToUserByName(userId, roleName);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario o rol no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Gamificación ====================

    /**
     * Agregar puntos a un usuario.
     *
     * @param userId ID del usuario
     * @param points Puntos a agregar (puede ser negativo para restar)
     * @return usuario actualizado
     */
    @PostMapping("/{userId}/points")
    @Operation(summary = "Agregar puntos", description = "Agrega puntos al usuario (gamificación)")
    public ResponseEntity<ApiResponse<UserRepresentation>> addPoints(@PathVariable String userId,
            @RequestParam Long points) {
        try {
            User updatedUser = userService.addPoints(userId, points);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Actualizar nivel de un usuario.
     *
     * @param userId   ID del usuario
     * @param newLevel Nuevo nivel
     * @return usuario actualizado
     */
    @PatchMapping("/{userId}/level")
    @Operation(summary = "Actualizar nivel", description = "Actualiza el nivel del usuario (gamificación)")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateLevel(@PathVariable String userId,
            @RequestParam Integer newLevel) {
        try {
            User updatedUser = userService.updateLevel(userId, newLevel);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Favoritos ====================

    /**
     * Agregar servicio a favoritos.
     *
     * @param userId    ID del usuario
     * @param serviceId ID del servicio
     * @return usuario actualizado
     */
    @PostMapping("/{userId}/favorites/{serviceId}")
    @Operation(summary = "Agregar favorito", description = "Agrega un servicio a la lista de favoritos del usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> addFavorite(@PathVariable String userId,
            @PathVariable Integer serviceId) {
        try {
            User updatedUser = userService.addFavorite(userId, serviceId);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("ya está")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage(), HttpStatus.CONFLICT.value()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Remover servicio de favoritos.
     *
     * @param userId    ID del usuario
     * @param serviceId ID del servicio
     * @return usuario actualizado
     */
    @DeleteMapping("/{userId}/favorites/{serviceId}")
    @Operation(summary = "Remover favorito", description = "Remueve un servicio de la lista de favoritos del usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> removeFavorite(@PathVariable String userId,
            @PathVariable Integer serviceId) {
        try {
            User updatedUser = userService.removeFavorite(userId, serviceId);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("no está")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Obtener lista de favoritos del usuario.
     *
     * @param userId ID del usuario
     * @return lista de IDs de servicios favoritos
     */
    @GetMapping("/{userId}/favorites")
    @Operation(summary = "Obtener favoritos", description = "Obtiene la lista de servicios favoritos del usuario")
    public ResponseEntity<ApiResponse<List<Integer>>> getFavorites(@PathVariable String userId) {
        try {
            List<Integer> favorites = userService.getFavorites(userId);
            return ResponseEntity.ok(ApiResponse.success(favorites));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Tracking de Compras ====================

    /**
     * Registrar una compra del usuario.
     *
     * @param userId ID del usuario
     * @param amount Monto de la compra
     * @return usuario actualizado
     */
    @PostMapping("/{userId}/purchase")
    @Operation(summary = "Registrar compra", description = "Registra una compra realizada por el usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> recordPurchase(@PathVariable String userId,
            @RequestParam BigDecimal amount) {
        try {
            User updatedUser = userService.recordPurchase(userId, amount);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Tracking de Reviews ====================

    /**
     * Registrar una review del usuario.
     *
     * @param userId ID del usuario
     * @param rating Calificación (1.0 - 5.0)
     * @return usuario actualizado
     */
    @PostMapping("/{userId}/review")
    @Operation(summary = "Registrar review", description = "Registra una review hecha por el usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> recordReview(@PathVariable String userId,
            @RequestParam Double rating) {
        try {
            if (rating < 1.0 || rating > 5.0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La calificación debe estar entre 1.0 y 5.0",
                                HttpStatus.BAD_REQUEST.value()));
            }

            User updatedUser = userService.recordReview(userId, rating);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== Preferencias ====================

    /**
     * Actualizar preferencias del usuario.
     *
     * @param userId      ID del usuario
     * @param preferences Nuevas preferencias
     * @return usuario actualizado
     */
    @PatchMapping("/{userId}/preferences")
    @Operation(summary = "Actualizar preferencias", description = "Actualiza las preferencias del usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> updatePreferences(@PathVariable String userId,
            @RequestBody UserPreferences preferences) {
        try {
            User updatedUser = userService.updatePreferences(userId, preferences);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Actualizar dirección del usuario.
     *
     * @param userId  ID del usuario
     * @param address Nueva dirección
     * @return usuario actualizado
     */
    @PatchMapping("/{userId}/address")
    @Operation(summary = "Actualizar dirección", description = "Actualiza la dirección del usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateAddress(@PathVariable String userId,
            @RequestBody Address address) {
        try {
            User updatedUser = userService.updateAddress(userId, address);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Actualizar metadatos del usuario.
     *
     * @param userId   ID del usuario
     * @param metadata Nuevos metadatos
     * @return usuario actualizado
     */
    @PatchMapping("/{userId}/metadata")
    @Operation(summary = "Actualizar metadatos", description = "Actualiza los metadatos del usuario")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateMetadata(@PathVariable String userId,
            @RequestBody Metadata metadata) {
        try {
            User updatedUser = userService.updateMetadata(userId, metadata);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    // ==================== API Root ====================

    /**
     * Endpoint raíz que proporciona enlaces de navegación de la API.
     *
     * @return Enlaces principales de la API de usuarios
     */
    @GetMapping("/api")
    @Operation(summary = "Obtener enlaces de la API", description = "Proporciona los enlaces principales de navegación de la API de usuarios")
    public ResponseEntity<ApiResponse<ApiRootDto>> getApiRoot() {
        try {
            ApiRootDto apiRoot = new ApiRootDto();

            // Set all the links
            apiRoot.getLinks().setGetAllUsers(linkTo(UserController.class).toString());
            apiRoot.getLinks().setGetPaginatedUsers(linkTo(methodOn(UserController.class)
                    .getUsersPaginated(10, null, null, null)).toString());
            apiRoot.getLinks().setSearchUsers(linkTo(methodOn(UserController.class)
                    .searchUsers("{query}", 10, null)).toString());
            apiRoot.getLinks().setGetUserById(linkTo(methodOn(UserController.class)
                    .getUserById("{userId}")).toString());
            apiRoot.getLinks().setGetUserByEmail(linkTo(methodOn(UserController.class)
                    .getUserByEmail("{email}")).toString());
            apiRoot.getLinks().setRegisterUser(linkTo(methodOn(UserController.class)
                    .registerUser(null)).toString());
            apiRoot.getLinks().setGetUserStats(linkTo(methodOn(UserController.class)
                    .getUserStats()).toString());
            apiRoot.getLinks().setGetRoles(linkTo(RoleController.class).toString());

            return ResponseEntity.ok(ApiResponse.success(apiRoot));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener enlaces de la API: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
