package cl.duoc.lunari.api.inventory.controller;

import cl.duoc.lunari.api.inventory.model.Categoria;
import cl.duoc.lunari.api.inventory.service.CategoriaService;
import cl.duoc.lunari.api.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "API de gestión de categorías de productos")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Obtener todas las categorías")
    public ResponseEntity<ApiResponse<List<Categoria>>> getAllCategorias() {
        List<Categoria> categorias = categoriaService.findAll();
        return ResponseEntity.ok(ApiResponse.success(categorias));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<ApiResponse<Categoria>> getCategoriaById(@PathVariable String id) {
        Optional<Categoria> categoria = categoriaService.findById(id);
        if (categoria.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(categoria.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Categoría no encontrada", HttpStatus.NOT_FOUND.value()));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar categorías por nombre")
    public ResponseEntity<ApiResponse<List<Categoria>>> buscarCategorias(
            @RequestParam String nombre) {
        List<Categoria> categorias = categoriaService.findByNombreContaining(nombre);
        return ResponseEntity.ok(ApiResponse.success(categorias));
    }

    @PostMapping
    @Operation(summary = "Crear nueva categoría")
    public ResponseEntity<ApiResponse<Categoria>> createCategoria(@RequestBody Categoria categoria) {
        try {
            Categoria nuevaCategoria = categoriaService.save(categoria);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(nuevaCategoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al crear categoría: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    public ResponseEntity<ApiResponse<Categoria>> updateCategoria(
            @PathVariable String id,
            @RequestBody Categoria categoria) {
        try {
            Optional<Categoria> existingCategoria = categoriaService.findById(id);
            if (existingCategoria.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Categoría no encontrada", HttpStatus.NOT_FOUND.value()));
            }
            categoria.setIdCategoria(id);
            Categoria updatedCategoria = categoriaService.save(categoria);
            return ResponseEntity.ok(ApiResponse.success(updatedCategoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al actualizar categoría: " + e.getMessage(),
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    public ResponseEntity<ApiResponse<Void>> deleteCategoria(@PathVariable String id) {
        try {
            Optional<Categoria> categoria = categoriaService.findById(id);
            if (categoria.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Categoría no encontrada", HttpStatus.NOT_FOUND.value()));
            }
            categoriaService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al eliminar categoría: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
