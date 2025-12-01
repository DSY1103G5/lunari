package cl.duoc.lunari.api.user.controller;

import cl.duoc.lunari.api.user.model.EmbeddedRole;
import cl.duoc.lunari.api.user.service.RoleService;
import cl.duoc.lunari.api.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gestión de roles predefinidos.
 *
 * Los roles ya no están en base de datos, sino que son constantes
 * definidas en el enum RoleType. Este controlador proporciona acceso
 * de solo lectura a esos roles predefinidos.
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Roles predefinidos del sistema")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Obtiene todos los roles disponibles.
     *
     * @return Lista de todos los roles (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Obtener todos los roles", description = "Obtiene la lista completa de roles disponibles en el sistema")
    public ResponseEntity<ApiResponse<List<EmbeddedRole>>> getAllRoles() {
        try {
            List<EmbeddedRole> roles = roleService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener roles: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene un rol por su ID.
     *
     * @param roleId ID del rol (1=ADMIN, 2=PRODUCT_OWNER, 3=CLIENT, 4=DEVOPS)
     * @return El rol si existe (HTTP 200), o HTTP 404 si no se encuentra
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "Obtener rol por ID", description = "Obtiene un rol específico usando su ID")
    public ResponseEntity<ApiResponse<EmbeddedRole>> getRoleById(@PathVariable Integer roleId) {
        try {
            Optional<EmbeddedRole> roleOptional = roleService.getRoleById(roleId);
            if (roleOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(roleOptional.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Rol no encontrado con ID: " + roleId,
                                HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener rol: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene un rol por su nombre.
     *
     * @param roleName Nombre del rol (ADMIN, PRODUCT_OWNER, CLIENT, DEVOPS)
     * @return El rol si existe (HTTP 200), o HTTP 404 si no se encuentra
     */
    @GetMapping("/name/{roleName}")
    @Operation(summary = "Obtener rol por nombre", description = "Obtiene un rol específico usando su nombre")
    public ResponseEntity<ApiResponse<EmbeddedRole>> getRoleByName(@PathVariable String roleName) {
        try {
            Optional<EmbeddedRole> roleOptional = roleService.getRoleByName(roleName);
            if (roleOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(roleOptional.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Rol no encontrado con nombre: " + roleName,
                                HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener rol: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene el rol por defecto para nuevos usuarios.
     *
     * @return Rol CLIENT (HTTP 200)
     */
    @GetMapping("/default")
    @Operation(summary = "Obtener rol por defecto", description = "Obtiene el rol asignado por defecto a nuevos usuarios")
    public ResponseEntity<ApiResponse<EmbeddedRole>> getDefaultRole() {
        try {
            EmbeddedRole defaultRole = roleService.getDefaultRole();
            return ResponseEntity.ok(ApiResponse.success(defaultRole));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener rol por defecto: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Verifica si existe un rol con el ID dado.
     *
     * @param roleId ID del rol
     * @return true si existe, false si no (HTTP 200)
     */
    @GetMapping("/{roleId}/exists")
    @Operation(summary = "Verificar existencia de rol por ID", description = "Verifica si existe un rol con el ID especificado")
    public ResponseEntity<ApiResponse<Boolean>> existsById(@PathVariable Integer roleId) {
        try {
            boolean exists = roleService.existsById(roleId);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al verificar rol: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Verifica si existe un rol con el nombre dado.
     *
     * @param roleName Nombre del rol
     * @return true si existe, false si no (HTTP 200)
     */
    @GetMapping("/name/{roleName}/exists")
    @Operation(summary = "Verificar existencia de rol por nombre", description = "Verifica si existe un rol con el nombre especificado")
    public ResponseEntity<ApiResponse<Boolean>> existsByName(@PathVariable String roleName) {
        try {
            boolean exists = roleService.existsByName(roleName);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al verificar rol: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
