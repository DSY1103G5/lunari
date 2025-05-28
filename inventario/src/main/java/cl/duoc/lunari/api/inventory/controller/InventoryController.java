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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final CatalogoService catalogoService;
    private final CategoriaService categoriaService;
    private final TipoRecursoService tipoRecursoService;
    private final PaqueteRecursoService paqueteRecursoService;

    @Autowired
    public InventoryController(CatalogoService catalogoService, 
                             CategoriaService categoriaService,
                             TipoRecursoService tipoRecursoService,
                             PaqueteRecursoService paqueteRecursoService) {
        this.catalogoService = catalogoService;
        this.categoriaService = categoriaService;
        this.tipoRecursoService = tipoRecursoService;
        this.paqueteRecursoService = paqueteRecursoService;
    }

    // ============= CATALOGO ENDPOINTS =============

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
    public ResponseEntity<ApiResponse<Catalogo>> getCatalogoById(@PathVariable Integer id) {
        Optional<Catalogo> catalogoOptional = catalogoService.findById(id);
        return ResponseUtil.fromOptional(catalogoOptional, "Servicio no encontrado");
    }

    /**
     * Busca servicios por nombre
     */
    @GetMapping("/catalogo/buscar")
    public ResponseEntity<ApiResponse<List<Catalogo>>> buscarServiciosPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El nombre no puede estar vacío", HttpStatus.BAD_REQUEST.value()));
        }
        
        try {
            List<Catalogo> catalogos = catalogoService.findByNombreContaining(nombre.trim());
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
    public ResponseEntity<ApiResponse<Catalogo>> activarServicio(@PathVariable Integer id) {
        try {
            Catalogo servicioActivado = catalogoService.activar(id);
            if (servicioActivado != null) {
                return ResponseEntity.ok(ApiResponse.success(servicioActivado));
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
    public ResponseEntity<ApiResponse<Catalogo>> desactivarServicio(@PathVariable Integer id) {
        try {
            Catalogo servicioDesactivado = catalogoService.desactivar(id);
            if (servicioDesactivado != null) {
                return ResponseEntity.ok(ApiResponse.success(servicioDesactivado));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Servicio no encontrado", HttpStatus.NOT_FOUND.value()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al desactivar servicio", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ============= CATEGORIAS ENDPOINTS =============

    /**
     * Obtiene todas las categorías
     */
    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<Categoria>>> getAllCategorias() {
        try {
            List<Categoria> categorias = categoriaService.findAll();
            return ResponseEntity.ok(ApiResponse.success(categorias));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener categorías", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene una categoría por ID
     */
    @GetMapping("/categorias/{id}")
    public ResponseEntity<ApiResponse<Categoria>> getCategoriaById(@PathVariable Integer id) {
        Optional<Categoria> categoriaOptional = categoriaService.findById(id);
        return ResponseUtil.fromOptional(categoriaOptional, "Categoría no encontrada");
    }

    /**
     * Busca categorías por nombre
     */
    @GetMapping("/categorias/buscar")
    public ResponseEntity<ApiResponse<List<Categoria>>> buscarCategoriasPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El nombre no puede estar vacío", HttpStatus.BAD_REQUEST.value()));
        }
        
        try {
            List<Categoria> categorias = categoriaService.findByNombreContaining(nombre.trim());
            return ResponseEntity.ok(ApiResponse.success(categorias));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar categorías", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Crea una nueva categoría
     */
    @PostMapping("/categorias")
    public ResponseEntity<ApiResponse<Categoria>> crearCategoria(@RequestBody Categoria categoria) {
        try {
            Categoria nuevaCategoria = categoriaService.save(categoria);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(nuevaCategoria));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al crear categoría: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // ============= TIPOS DE RECURSO ENDPOINTS =============

    /**
     * Obtiene todos los tipos de recurso
     */
    @GetMapping("/tipos-recurso")
    public ResponseEntity<ApiResponse<List<TipoRecurso>>> getAllTiposRecurso() {
        try {
            List<TipoRecurso> tiposRecurso = tipoRecursoService.findAll();
            return ResponseEntity.ok(ApiResponse.success(tiposRecurso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener tipos de recurso", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene un tipo de recurso por ID
     */
    @GetMapping("/tipos-recurso/{id}")
    public ResponseEntity<ApiResponse<TipoRecurso>> getTipoRecursoById(@PathVariable Integer id) {
        Optional<TipoRecurso> tipoRecursoOptional = tipoRecursoService.findById(id);
        return ResponseUtil.fromOptional(tipoRecursoOptional, "Tipo de recurso no encontrado");
    }

    /**
     * Busca tipos de recurso por unidad de medida
     */
    @GetMapping("/tipos-recurso/unidad-medida")
    public ResponseEntity<ApiResponse<List<TipoRecurso>>> getTiposRecursoPorUnidadMedida(@RequestParam String unidadMedida) {
        if (unidadMedida == null || unidadMedida.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("La unidad de medida no puede estar vacía", HttpStatus.BAD_REQUEST.value()));
        }
        
        try {
            List<TipoRecurso> tiposRecurso = tipoRecursoService.findByUnidadMedida(unidadMedida.trim());
            return ResponseEntity.ok(ApiResponse.success(tiposRecurso));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar tipos de recurso", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Crea un nuevo tipo de recurso
     */
    @PostMapping("/tipos-recurso")
    public ResponseEntity<ApiResponse<TipoRecurso>> crearTipoRecurso(@RequestBody TipoRecurso tipoRecurso) {
        try {
            TipoRecurso nuevoTipoRecurso = tipoRecursoService.save(tipoRecurso);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(nuevoTipoRecurso));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al crear tipo de recurso: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // ============= PAQUETES RECURSO SERVICIO ENDPOINTS =============

    /**
     * Obtiene recursos asociados a un servicio específico
     */
    @GetMapping("/servicios/{servicioId}/recursos")
    public ResponseEntity<ApiResponse<List<PaqueteRecursoServicio>>> getRecursosPorServicio(@PathVariable Integer servicioId) {
        try {
            List<PaqueteRecursoServicio> recursos = paqueteRecursoService.findByServicio(servicioId);
            return ResponseEntity.ok(ApiResponse.success(recursos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener recursos del servicio", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene servicios que utilizan un tipo de recurso específico
     */
    @GetMapping("/tipos-recurso/{tipoRecursoId}/servicios")
    public ResponseEntity<ApiResponse<List<PaqueteRecursoServicio>>> getServiciosPorTipoRecurso(@PathVariable Integer tipoRecursoId) {
        try {
            List<PaqueteRecursoServicio> paquetes = paqueteRecursoService.findByTipoRecurso(tipoRecursoId);
            return ResponseEntity.ok(ApiResponse.success(paquetes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener servicios por tipo de recurso", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Calcula el costo total de recursos para un servicio
     */
    @GetMapping("/servicios/{servicioId}/costo-recursos")
    public ResponseEntity<ApiResponse<BigDecimal>> calcularCostoRecursos(@PathVariable Integer servicioId) {
        try {
            BigDecimal costo = paqueteRecursoService.calcularCostoRecursos(servicioId);
            return ResponseEntity.ok(ApiResponse.success(costo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al calcular costo de recursos", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Asocia un recurso a un servicio
     */
    @PostMapping("/servicios/{servicioId}/recursos")
    public ResponseEntity<ApiResponse<PaqueteRecursoServicio>> asociarRecursoAServicio(
            @PathVariable Integer servicioId, 
            @RequestBody PaqueteRecursoServicio paqueteRecurso) {
        try {
            PaqueteRecursoServicio nuevoPaquete = paqueteRecursoService.save(paqueteRecurso);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(nuevoPaquete));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al asociar recurso al servicio: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Elimina la asociación de un recurso con un servicio
     */
    @DeleteMapping("/paquetes-recurso/{paqueteId}")
    public ResponseEntity<ApiResponse<Void>> eliminarPaqueteRecurso(@PathVariable Integer paqueteId) {
        try {
            paqueteRecursoService.deleteById(paqueteId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Paquete de recurso no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }
}