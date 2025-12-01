package cl.duoc.lunari.api.cart.controller;

import cl.duoc.lunari.api.cart.dto.PedidoResponseDto;
import cl.duoc.lunari.api.cart.model.EstadoPedido;
import cl.duoc.lunari.api.cart.model.Pedido;
import cl.duoc.lunari.api.cart.service.OrderService;
import cl.duoc.lunari.api.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de pedidos
 * Proporciona operaciones CRUD para consultar y gestionar pedidos
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "API para gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    /**
     * Obtiene un pedido por su ID
     *
     * @param orderId ID del pedido
     * @return Pedido encontrado
     */
    @GetMapping("/{orderId}")
    @Operation(
        summary = "Obtener pedido por ID",
        description = "Retorna los detalles completos de un pedido incluyendo items y estado de pago"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado",
            content = @Content(schema = @Schema(implementation = PedidoResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<PedidoResponseDto>> getOrderById(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable UUID orderId
    ) {
        try {
            log.debug("Obteniendo pedido por ID: {}", orderId);

            Pedido pedido = orderService.getOrderById(orderId);
            PedidoResponseDto response = PedidoResponseDto.fromEntity(pedido);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            log.warn("Pedido no encontrado: {}", orderId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Pedido no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Obtiene un pedido por su número de pedido
     *
     * @param numeroOrden Número de pedido (formato: ORD-YYYYMMDD-XXXXX)
     * @return Pedido encontrado
     */
    @GetMapping("/numero/{numeroOrden}")
    @Operation(
        summary = "Obtener pedido por número",
        description = "Retorna un pedido buscando por su número único (ej: ORD-20250130-00001)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<PedidoResponseDto>> getOrderByNumero(
            @Parameter(description = "Número del pedido", required = true)
            @PathVariable String numeroOrden
    ) {
        try {
            log.debug("Obteniendo pedido por número: {}", numeroOrden);

            Pedido pedido = orderService.getOrderByNumero(numeroOrden);
            PedidoResponseDto response = PedidoResponseDto.fromEntity(pedido);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            log.warn("Pedido no encontrado: {}", numeroOrden);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Pedido no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Obtiene todos los pedidos de un usuario
     *
     * @param usuarioId ID del usuario
     * @return Lista de pedidos del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    @Operation(
        summary = "Obtener pedidos de un usuario",
        description = "Retorna todos los pedidos de un usuario ordenados por fecha de creación descendente"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos retornada exitosamente"
        )
    })
    public ResponseEntity<ApiResponse<List<PedidoResponseDto>>> getOrdersByUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable UUID usuarioId
    ) {
        log.debug("Obteniendo pedidos del usuario: {}", usuarioId);

        List<Pedido> pedidos = orderService.getOrdersByUsuario(usuarioId);
        List<PedidoResponseDto> response = pedidos.stream()
                .map(PedidoResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Obtiene pedidos de un usuario filtrados por estado
     *
     * @param usuarioId ID del usuario
     * @param estado Estado del pedido
     * @return Lista de pedidos filtrados
     */
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    @Operation(
        summary = "Obtener pedidos por usuario y estado",
        description = "Retorna los pedidos de un usuario filtrados por estado específico"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos filtrados"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado inválido"
        )
    })
    public ResponseEntity<ApiResponse<List<PedidoResponseDto>>> getOrdersByUsuarioAndEstado(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable UUID usuarioId,
            @Parameter(description = "Estado del pedido (CREADO, PAGO_PENDIENTE, PAGO_COMPLETADO, PROCESANDO, COMPLETADO, CANCELADO, FALLIDO)", required = true)
            @PathVariable String estado
    ) {
        try {
            log.debug("Obteniendo pedidos del usuario {} con estado: {}", usuarioId, estado);

            EstadoPedido estadoPedido = EstadoPedido.valueOf(estado.toUpperCase());
            List<Pedido> pedidos = orderService.getOrdersByUsuarioAndEstado(usuarioId, estadoPedido);
            List<PedidoResponseDto> response = pedidos.stream()
                    .map(PedidoResponseDto::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Estado de pedido inválido: {}", estado);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Estado de pedido inválido: " + estado,
                            HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza el estado de un pedido
     *
     * @param orderId ID del pedido
     * @param nuevoEstado Nuevo estado del pedido
     * @return Pedido actualizado
     */
    @PutMapping("/{orderId}/estado")
    @Operation(
        summary = "Actualizar estado del pedido",
        description = "Actualiza el estado de un pedido. Uso restringido para administradores."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado inválido o transición no permitida"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<PedidoResponseDto>> updateOrderStatus(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable UUID orderId,
            @Parameter(description = "Nuevo estado", required = true)
            @RequestParam String nuevoEstado
    ) {
        try {
            log.info("Actualizando estado del pedido {} a {}", orderId, nuevoEstado);

            EstadoPedido estadoPedido = EstadoPedido.valueOf(nuevoEstado.toUpperCase());
            Pedido pedido = orderService.updateOrderStatus(orderId, estadoPedido);
            PedidoResponseDto response = PedidoResponseDto.fromEntity(pedido);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Estado de pedido inválido: {}", nuevoEstado);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Estado inválido: " + nuevoEstado,
                            HttpStatus.BAD_REQUEST.value()));

        } catch (RuntimeException e) {
            log.error("Error al actualizar estado del pedido: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Cancela un pedido
     *
     * @param orderId ID del pedido
     * @return Pedido cancelado
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(
        summary = "Cancelar pedido",
        description = "Cancela un pedido si aún no ha sido completado"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido cancelado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "No se puede cancelar el pedido en su estado actual"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<PedidoResponseDto>> cancelOrder(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable UUID orderId
    ) {
        try {
            log.info("Cancelando pedido: {}", orderId);

            Pedido pedido = orderService.cancelOrder(orderId);
            PedidoResponseDto response = PedidoResponseDto.fromEntity(pedido);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalStateException e) {
            log.warn("No se puede cancelar el pedido {}: {}", orderId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));

        } catch (RuntimeException e) {
            log.error("Error al cancelar pedido: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
