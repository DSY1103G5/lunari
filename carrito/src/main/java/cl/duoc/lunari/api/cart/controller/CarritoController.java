package cl.duoc.lunari.api.cart.controller;

import cl.duoc.lunari.api.cart.dto.*;
import cl.duoc.lunari.api.cart.model.Carrito;
import cl.duoc.lunari.api.cart.model.CarritoItem;
import cl.duoc.lunari.api.cart.service.CarritoService;
import cl.duoc.lunari.api.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CarritoController {

    private final CarritoService carritoService;

    /**
     * Obtiene el carrito activo de un usuario o crea uno nuevo
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<CarritoResponseDto>> obtenerCarritoUsuario(@PathVariable UUID usuarioId) {
        try {
            Carrito carrito = carritoService.obtenerOCrearCarritoActivo(usuarioId);
            CarritoResponseDto response = CarritoResponseDto.fromEntity(carrito);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al obtener carrito: " + e.getMessage(), 
                          HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtiene un carrito por ID
     */
    @GetMapping("/{carritoId}")
    public ResponseEntity<ApiResponse<CarritoResponseDto>> obtenerCarritoPorId(@PathVariable UUID carritoId) {
        try {
            Carrito carrito = carritoService.obtenerCarritoPorId(carritoId);
            CarritoResponseDto response = CarritoResponseDto.fromEntity(carrito);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Carrito no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Obtiene todos los carritos de un usuario
     */
    @GetMapping("/usuario/{usuarioId}/todos")
    public ResponseEntity<ApiResponse<List<CarritoResponseDto>>> obtenerCarritosPorUsuario(@PathVariable UUID usuarioId) {
        try {
            List<Carrito> carritos = carritoService.obtenerCarritosPorUsuario(usuarioId);
            List<CarritoResponseDto> response = carritos.stream()
                    .map(CarritoResponseDto::fromEntity)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener carritos", 
                          HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Agrega un item al carrito
     */
    @PostMapping("/{carritoId}/items")
    public ResponseEntity<ApiResponse<CarritoItemResponseDto>> agregarItem(
            @PathVariable UUID carritoId,
            @RequestBody AgregarItemRequestDto request) {
        try {
            CarritoItem item = carritoService.agregarItemAlCarrito(
                    carritoId, 
                    request.getServicioId(), 
                    request.getCantidad(), 
                    request.getPersonalizaciones()
            );
            CarritoItemResponseDto response = CarritoItemResponseDto.fromEntity(item);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al agregar item: " + e.getMessage(), 
                          HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza la cantidad de un item
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CarritoItemResponseDto>> actualizarItem(
            @PathVariable UUID itemId,
            @RequestBody ActualizarCantidadRequestDto request) {
        try {
            CarritoItem item = carritoService.actualizarCantidadItem(itemId, request.getCantidad());
            
            if (item == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.success(null));
            }
            
            CarritoItemResponseDto response = CarritoItemResponseDto.fromEntity(item);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al actualizar item: " + e.getMessage(), 
                          HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Elimina un item del carrito
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> eliminarItem(@PathVariable UUID itemId) {
        try {
            carritoService.eliminarItemDelCarrito(itemId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Item no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Procesa el carrito (finaliza la compra)
     */
    @PutMapping("/{carritoId}/procesar")
    public ResponseEntity<ApiResponse<CarritoResponseDto>> procesarCarrito(@PathVariable UUID carritoId) {
        try {
            Carrito carrito = carritoService.procesarCarrito(carritoId);
            CarritoResponseDto response = CarritoResponseDto.fromEntity(carrito);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al procesar carrito: " + e.getMessage(), 
                          HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Vacía el carrito
     */
    @DeleteMapping("/{carritoId}/vaciar")
    public ResponseEntity<ApiResponse<Void>> vaciarCarrito(@PathVariable UUID carritoId) {
        try {
            carritoService.vaciarCarrito(carritoId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error al vaciar carrito: " + e.getMessage(), 
                          HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtiene estadísticas básicas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<CarritoEstadisticasResponseDto>> obtenerEstadisticas() {
        try {
            CarritoService.CarritoEstadisticas estadisticas = carritoService.obtenerEstadisticas();
            CarritoEstadisticasResponseDto response = CarritoEstadisticasResponseDto.fromStats(estadisticas);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener estadísticas", 
                          HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}