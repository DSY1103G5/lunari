package cl.duoc.lunari.api.inventory.controller;

import cl.duoc.lunari.api.inventory.model.Catalogo;
import cl.duoc.lunari.api.inventory.model.Categoria;
import cl.duoc.lunari.api.inventory.model.TipoRecurso;
import cl.duoc.lunari.api.inventory.model.PaqueteRecursoServicio;
import cl.duoc.lunari.api.inventory.service.CatalogoService;
import cl.duoc.lunari.api.inventory.service.CategoriaService;
import cl.duoc.lunari.api.inventory.service.TipoRecursoService;
import cl.duoc.lunari.api.inventory.service.PaqueteRecursoService;
import cl.duoc.lunari.api.payload.ApiResponse;
import cl.duoc.lunari.api.payload.ResponseUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final CatalogoService catalogoService;

    @Autowired
    public InventoryController(CatalogoService catalogoService, 
                             CategoriaService categoriaService,
                             TipoRecursoService tipoRecursoService,
                             PaqueteRecursoService paqueteRecursoService) {
        this.catalogoService = catalogoService;
    }

    /**
     * Obtiene todos los servicios del catálogo
     */
    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<List<Catalogo>>> getAllCatalogo() {
        try {
            List<Catalogo> catalogos = catalogoService.findAll();
            return ResponseEntity.ok(ApiResponse.success(catalogos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener catálogo", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }


    /**
     * Obtiene solo los servicios activos del catálogo
     */
    @GetMapping("/catalogo/activos")
    public ResponseEntity<ApiResponse<List<Catalogo>>> getCatalogoActivos() {
        try {
            List<Catalogo> catalogos = catalogoService.findAllActivos();
            return ResponseEntity.ok(ApiResponse.success(catalogos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener catálogo activo", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }   
    
    
    /**
     * Obtiene un servicio específico por ID
     */
    @GetMapping("/catalogo/{id}")
    public ResponseEntity<ApiResponse<Catalogo>> getCatalogoById(@PathVariable String id) {
        try {
            Integer idNumerico = Integer.parseInt(id);
            Optional<Catalogo> catalogoOptional = catalogoService.findById(idNumerico);
            if (catalogoOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(catalogoOptional.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Servicio no encontrado", HttpStatus.NOT_FOUND.value()));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("ID debe ser un número válido", HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener servicio", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }


    /**
     * Busca servicios por nombre
     */    @GetMapping("/catalogo/buscar")
    public ResponseEntity<ApiResponse<List<Catalogo>>> buscarServiciosPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El nombre no puede estar vacío", HttpStatus.BAD_REQUEST.value()));
        }
        
        try {
            List<Catalogo> catalogos = catalogoService.findByNombreContaining(nombre.trim());
            if (catalogos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No se encontraron servicios con el nombre especificado", HttpStatus.NOT_FOUND.value()));
            }
            return ResponseEntity.ok(ApiResponse.success(catalogos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar servicios", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene servicios por categoría
     */
    @GetMapping("/catalogo/categoria/{categoriaId}")
    public ResponseEntity<ApiResponse<List<Catalogo>>> getServiciosPorCategoria(@PathVariable Integer categoriaId) {
        try {
            List<Catalogo> catalogos = catalogoService.findByCategoria(categoriaId);
            return ResponseEntity.ok(ApiResponse.success(catalogos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener servicios por categoría", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }   
    
    /**
     * Crea un nuevo servicio en el catálogo
     */
    @PostMapping("/catalogo")
    public ResponseEntity<ApiResponse<Catalogo>> crearServicio(@RequestBody Catalogo catalogo) {
        try {
            Catalogo nuevoServicio = catalogoService.save(catalogo);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(nuevoServicio));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al crear servicio: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno del servidor al crear servicio", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }


    /**
     * Actualiza un servicio existente
     */
    @PutMapping("/catalogo/{id}")
    public ResponseEntity<ApiResponse<Catalogo>> actualizarServicio(@PathVariable Integer id, @RequestBody Catalogo catalogoDetails) {
        try {
            Optional<Catalogo> catalogoExistente = catalogoService.findById(id);
            if (catalogoExistente.isPresent()) {
                catalogoDetails.setIdServicio(id);
                Catalogo servicioActualizado = catalogoService.save(catalogoDetails);
                return ResponseEntity.ok(ApiResponse.success(servicioActualizado));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Servicio no encontrado", HttpStatus.NOT_FOUND.value()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al actualizar servicio: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }


    /**
     * Activa un servicio
     */
    @PatchMapping("/catalogo/{id}/activar")
    public ResponseEntity<ApiResponse<String>> activarServicio(@PathVariable Integer id) {
        try {
            Catalogo servicioActivado = catalogoService.activar(id);
            if (servicioActivado != null) {
                return ResponseEntity.ok(ApiResponse.success("Servicio activado correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Servicio no encontrado", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al activar servicio", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }


    /**
     * Desactiva un servicio
     */
    @PatchMapping("/catalogo/{id}/desactivar")
    public ResponseEntity<ApiResponse<String>> desactivarServicio(@PathVariable Integer id) {
        try {
            Catalogo servicioDesactivado = catalogoService.desactivar(id);
            if (servicioDesactivado != null) {
                return ResponseEntity.ok(ApiResponse.success("Servicio desactivado correctamente"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Servicio no encontrado", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al desactivar servicio", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Elimina un servicio del catálogo
     */
    @DeleteMapping("/catalogo/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        try {
            catalogoService.deleteCatalogo(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null));
        } catch (RuntimeException e) { // Replace with specific exception
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Servicio no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }





   /**
     * Maneja errores de deserialización JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        String errorMessage = "Error en formato JSON: " + ex.getMostSpecificCause().getMessage();
        
        if (ex.getMessage().contains("Cannot deserialize")) {
            errorMessage = "Error en el formato de los datos JSON. Verifique que todos los campos tengan el tipo correcto.";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorMessage, HttpStatus.BAD_REQUEST.value()));
    }
}