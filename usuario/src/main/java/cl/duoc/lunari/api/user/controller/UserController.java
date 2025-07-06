package cl.duoc.lunari.api.user.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.model.UserRole;
import cl.duoc.lunari.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.dto.PagedUserRepresentation;
import cl.duoc.lunari.api.user.dto.ApiRootDto;
import cl.duoc.lunari.api.user.assembler.UserModelAssembler;
import cl.duoc.lunari.api.user.assembler.PagedUserModelAssembler;
import cl.duoc.lunari.api.payload.ApiResponse;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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

    /**
     * Devuelve todos los usuarios con paginación y ordenamiento mejorado.
     * 
     * @param page      número de página (0-indexed, default: 0)
     * @param size      tamaño de página (default: 10)
     * @param sort      campo y dirección de ordenamiento (ej: firstName,asc o
     *                  createdAt,desc)
     * @param active    filtro por estado activo (opcional)
     * @param roleId    filtro por rol (opcional)
     * @param companyId filtro por empresa (opcional)
     * @return Página de usuarios (HTTP 200)
     */
    @GetMapping("/paginated")
    @Operation(summary = "Obtener usuarios paginados", description = "Obtiene todos los usuarios con paginación, ordenamiento y filtros")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> getUsersPaginated(
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

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, active, roleId, companyId, sort);

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
     * @return Lista de usuarios (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene todos los usuarios de LUNARi")
    public ResponseEntity<ApiResponse<CollectionModel<UserRepresentation>>> getAllUsers(Pageable pageable) {

        try {
            List<User> users = userService.getAllUsers(pageable);
            List<UserRepresentation> userRepresentations = users.stream()
                    .map(userModelAssembler::toModel)
                    .toList();
                    
            CollectionModel<UserRepresentation> collectionModel = CollectionModel.of(userRepresentations);
            collectionModel.add(linkTo(UserController.class).withSelfRel());
            collectionModel.add(linkTo(methodOn(UserController.class)
                    .getUsersPaginated(0, 10, "createdAt,desc", null, null, null))
                    .withRel("paginated"));
            collectionModel.add(linkTo(methodOn(UserController.class)
                    .getAllRoles()).withRel("roles"));
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
     * @param id ID del usuario
     * @return El usuario si se encuentra (HTTP 200), o HTTP 404 si no se encuentra.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene un usuario específico usando su ID único")
    public ResponseEntity<ApiResponse<UserRepresentation>> getUserById(@PathVariable UUID id) {
        Optional<User> userOptional = userService.getUserById(id);
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
                    .body(ApiResponse.error("Erro al registrar usuario: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
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
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateUser(@PathVariable UUID id,
            @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
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
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema usando su ID")
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
     * @param page  número de página
     * @param size  tamaño de página
     * @return usuarios que coinciden con la búsqueda
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre o apellido con paginación")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> searchUsers(
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

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, null, null, null, "firstName,asc");

            return ResponseEntity.ok(ApiResponse.success(pagedRepresentation));
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
     * @param page      número de página
     * @param size      tamaño de página
     * @return usuarios de la empresa especificada
     */
    @GetMapping("/company/{companyId}")
    @Operation(summary = "Obtener usuarios por empresa", description = "Obtiene todos los usuarios pertenecientes a una empresa específica")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> getUsersByCompany(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
            Page<User> users = userService.getUsersByCompanyPaginated(companyId, pageable);

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, null, null, companyId, "firstName,asc");

            return ResponseEntity.ok(ApiResponse.success(pagedRepresentation));
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
     * @param page   número de página
     * @param size   tamaño de página
     * @return usuarios con el rol especificado
     */
    @GetMapping("/role/{roleId}")
    @Operation(summary = "Obtener usuarios por rol", description = "Obtiene todos los usuarios que tienen un rol específico")
    public ResponseEntity<ApiResponse<PagedUserRepresentation>> getUsersByRole(
            @PathVariable Integer roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
            Page<User> users = userService.getUsersByRolePaginated(roleId, pageable);

            PagedUserRepresentation pagedRepresentation = pagedUserModelAssembler.toModel(
                    users, null, roleId, null, "firstName,asc");

            return ResponseEntity.ok(ApiResponse.success(pagedRepresentation));
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

    /**
     * Activar/desactivar usuario.
     * 
     * @param id     ID del usuario
     * @param active estado activo
     * @return usuario actualizado
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de usuario", description = "Activa o desactiva un usuario en el sistema")
    public ResponseEntity<ApiResponse<UserRepresentation>> updateUserStatus(@PathVariable UUID id,
            @RequestParam Boolean active) {
        try {
            User updatedUser = userService.updateUserStatus(id, active);
            UserRepresentation userRepresentation = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(ApiResponse.success(userRepresentation));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Cambiar contraseña del usuario.
     * 
     * @param id          ID del usuario
     * @param newPassword nueva contraseña
     * @return confirmación del cambio
     */
    @PatchMapping("/{id}/password")
    @Operation(summary = "Cambiar contraseña", description = "Actualiza la contraseña de un usuario específico")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@PathVariable UUID id,
            @RequestBody String newPassword) {
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
    @Operation(summary = "Asignar rol a usuario", description = "Asigna un rol específico a un usuario")
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
     * @param userId    ID del usuario
     * @param companyId ID de la empresa
     * @return confirmación de la asignación
     */
    @PatchMapping("/{userId}/company/{companyId}")
    @Operation(summary = "Asignar usuario a empresa", description = "Asigna un usuario a una empresa específica")
    public ResponseEntity<ApiResponse<Void>> assignUserToCompany(@PathVariable UUID userId,
            @PathVariable UUID companyId) {
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

    /**
     * Obtener todos los roles disponibles.
     * 
     * @return lista de roles
     */
    @GetMapping("/roles")
    @Operation(summary = "Obtener todos los roles", description = "Obtiene la lista completa de roles disponibles en el sistema")
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
                    .getUsersPaginated(0, 10, "createdAt,desc", null, null, null)).toString());
            apiRoot.getLinks().setSearchUsers(linkTo(methodOn(UserController.class)
                    .searchUsers("{query}", 0, 10)).toString());
            apiRoot.getLinks().setGetUserById(linkTo(methodOn(UserController.class)
                    .getUserById(null)).toString().replace("null", "{id}"));
            apiRoot.getLinks().setGetUserByEmail(linkTo(methodOn(UserController.class)
                    .getUserByEmail("{email}")).toString());
            apiRoot.getLinks().setRegisterUser(linkTo(methodOn(UserController.class)
                    .registerUser(null)).toString());
            apiRoot.getLinks().setGetAllRoles(linkTo(methodOn(UserController.class)
                    .getAllRoles()).toString());
            apiRoot.getLinks().setGetUserStats(linkTo(methodOn(UserController.class)
                    .getUserStats()).toString());

            return ResponseEntity.ok(ApiResponse.success(apiRoot));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener enlaces de la API: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}