package cl.duoc.lunari.api.inventory.controller;

import cl.duoc.lunari.api.inventory.model.Producto;
import cl.duoc.lunari.api.inventory.service.ProductoService;
import cl.duoc.lunari.api.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "API de gestión de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Obtener todos los productos")
    public ResponseEntity<ApiResponse<List<Producto>>> getAllProductos() {
        List<Producto> productos = productoService.findAll();
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener productos activos")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosActivos() {
        List<Producto> productos = productoService.findActivos();
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ApiResponse<Producto>> getProductoById(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.findById(id);
        if (producto.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(producto.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Producto no encontrado", HttpStatus.NOT_FOUND.value()));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener producto por código")
    public ResponseEntity<ApiResponse<Producto>> getProductoByCode(@PathVariable String code) {
        Optional<Producto> producto = productoService.findByCode(code);
        if (producto.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(producto.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Producto no encontrado", HttpStatus.NOT_FOUND.value()));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos por nombre")
    public ResponseEntity<ApiResponse<List<Producto>>> buscarProductos(
            @RequestParam String nombre) {
        List<Producto> productos = productoService.findByNombreContaining(nombre);
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Obtener productos por categoría")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosByCategoria(
            @PathVariable String categoriaId) {
        List<Producto> productos = productoService.findByCategoria(categoriaId);
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/marca/{marca}")
    @Operation(summary = "Obtener productos por marca")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosByMarca(
            @PathVariable String marca) {
        List<Producto> productos = productoService.findByMarca(marca);
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/en-stock")
    @Operation(summary = "Obtener productos en stock")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosEnStock() {
        List<Producto> productos = productoService.findEnStock();
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/precio")
    @Operation(summary = "Obtener productos por rango de precio")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosByPrecio(
            @RequestParam Integer min,
            @RequestParam Integer max) {
        List<Producto> productos = productoService.findByPrecioRange(min, max);
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/rating")
    @Operation(summary = "Obtener productos por rating mínimo")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosByRating(
            @RequestParam BigDecimal min) {
        List<Producto> productos = productoService.findByMinRating(min);
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "Obtener productos por tag")
    public ResponseEntity<ApiResponse<List<Producto>>> getProductosByTag(
            @PathVariable String tag) {
        List<Producto> productos = productoService.findByTag(tag);
        return ResponseEntity.ok(ApiResponse.success(productos));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo producto")
    public ResponseEntity<ApiResponse<Producto>> createProducto(@RequestBody Producto producto) {
        try {
            Producto nuevoProducto = productoService.save(producto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(nuevoProducto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ApiResponse<Producto>> updateProducto(
            @PathVariable Integer id,
            @RequestBody Producto producto) {
        try {
            Optional<Producto> existingProducto = productoService.findById(id);
            if (existingProducto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Producto no encontrado", HttpStatus.NOT_FOUND.value()));
            }
            producto.setIdProducto(id);
            Producto updatedProducto = productoService.save(producto);
            return ResponseEntity.ok(ApiResponse.success(updatedProducto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar producto")
    public ResponseEntity<ApiResponse<Producto>> activarProducto(@PathVariable Integer id) {
        try {
            Producto producto = productoService.activar(id);
            return ResponseEntity.ok(ApiResponse.success(producto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar producto")
    public ResponseEntity<ApiResponse<Producto>> desactivarProducto(@PathVariable Integer id) {
        try {
            Producto producto = productoService.desactivar(id);
            return ResponseEntity.ok(ApiResponse.success(producto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Actualizar stock del producto")
    public ResponseEntity<ApiResponse<Producto>> actualizarStock(
            @PathVariable Integer id,
            @RequestParam Integer stock) {
        try {
            Producto producto = productoService.actualizarStock(id, stock);
            return ResponseEntity.ok(ApiResponse.success(producto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<ApiResponse<Void>> deleteProducto(@PathVariable Integer id) {
        try {
            productoService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
