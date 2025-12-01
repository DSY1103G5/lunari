package cl.duoc.lunari.api.cart.controller;

import cl.duoc.lunari.api.cart.dto.PagoResponseDto;
import cl.duoc.lunari.api.cart.model.Pago;
import cl.duoc.lunari.api.cart.service.PaymentService;
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

import java.util.UUID;

/**
 * Controlador REST para consultas de pagos
 * Proporciona endpoints para consultar el estado y detalles de pagos
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "API para consultas de pagos")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Obtiene un pago por su ID
     *
     * @param pagoId ID del pago
     * @return Detalles del pago
     */
    @GetMapping("/{pagoId}")
    @Operation(
        summary = "Obtener pago por ID",
        description = "Retorna los detalles completos de un pago incluyendo estado, método y datos de Transbank"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago encontrado",
            content = @Content(schema = @Schema(implementation = PagoResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<PagoResponseDto>> getPaymentById(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable UUID pagoId
    ) {
        try {
            log.debug("Obteniendo pago por ID: {}", pagoId);

            Pago pago = paymentService.getPaymentById(pagoId);
            PagoResponseDto response = PagoResponseDto.fromEntity(pago);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            log.warn("Pago no encontrado: {}", pagoId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Pago no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Obtiene un pago por ID de pedido
     *
     * @param pedidoId ID del pedido
     * @return Detalles del pago asociado al pedido
     */
    @GetMapping("/pedido/{pedidoId}")
    @Operation(
        summary = "Obtener pago por ID de pedido",
        description = "Retorna el pago asociado a un pedido específico"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No se encontró pago para el pedido"
        )
    })
    public ResponseEntity<ApiResponse<PagoResponseDto>> getPaymentByOrderId(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable UUID pedidoId
    ) {
        try {
            log.debug("Obteniendo pago por pedido ID: {}", pedidoId);

            Pago pago = paymentService.getPaymentByOrderId(pedidoId);
            PagoResponseDto response = PagoResponseDto.fromEntity(pago);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            log.warn("Pago no encontrado para pedido: {}", pedidoId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No se encontró pago para el pedido", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Obtiene un pago por token de Transbank
     *
     * @param token Token de Transbank
     * @return Detalles del pago
     */
    @GetMapping("/token/{token}")
    @Operation(
        summary = "Obtener pago por token de Transbank",
        description = "Retorna el pago asociado a un token específico de Transbank"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No se encontró pago con el token especificado"
        )
    })
    public ResponseEntity<ApiResponse<PagoResponseDto>> getPaymentByToken(
            @Parameter(description = "Token de Transbank", required = true)
            @PathVariable String token
    ) {
        try {
            log.debug("Obteniendo pago por token: {}", token);

            Pago pago = paymentService.getPaymentByToken(token);
            PagoResponseDto response = PagoResponseDto.fromEntity(pago);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            log.warn("Pago no encontrado para token: {}", token);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No se encontró pago con el token especificado",
                            HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Verifica si un pago fue aprobado
     *
     * @param pagoId ID del pago
     * @return Estado de aprobación del pago
     */
    @GetMapping("/{pagoId}/approved")
    @Operation(
        summary = "Verificar si pago fue aprobado",
        description = "Retorna true si el pago fue aprobado, false en caso contrario"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado verificado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<Boolean>> isPaymentApproved(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable UUID pagoId
    ) {
        try {
            log.debug("Verificando aprobación de pago: {}", pagoId);

            Pago pago = paymentService.getPaymentById(pagoId);
            boolean approved = pago.estaAprobado();

            return ResponseEntity.ok(ApiResponse.success(approved));

        } catch (RuntimeException e) {
            log.warn("Pago no encontrado: {}", pagoId);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Pago no encontrado", HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Endpoint de salud para verificar disponibilidad del servicio de pagos
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Verifica que el servicio de pagos esté disponible"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Servicio disponible"
        )
    })
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Payment service is running"));
    }
}
