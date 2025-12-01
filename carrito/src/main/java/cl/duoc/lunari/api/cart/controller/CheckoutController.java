package cl.duoc.lunari.api.cart.controller;

import cl.duoc.lunari.api.cart.dto.CheckoutInitiateRequest;
import cl.duoc.lunari.api.cart.dto.CheckoutInitiateResponse;
import cl.duoc.lunari.api.cart.dto.PedidoResponseDto;
import cl.duoc.lunari.api.cart.model.Pedido;
import cl.duoc.lunari.api.cart.service.CheckoutService;
import cl.duoc.lunari.api.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el proceso de checkout
 * Maneja la iniciación y confirmación de pagos con Transbank
 */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Checkout", description = "API para proceso de checkout y pagos")
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Inicia el proceso de checkout para un carrito
     * Crea un pedido, inicia el pago con Transbank y devuelve la URL de pago
     *
     * @param request Solicitud de checkout con ID de carrito y URL de retorno
     * @return Respuesta con URL de pago y token de Transbank
     */
    @PostMapping("/initiate")
    @Operation(
        summary = "Iniciar checkout",
        description = "Inicia el proceso de checkout creando un pedido e iniciando el pago con Transbank. " +
                     "Devuelve la URL a la que el usuario debe ser redirigido para completar el pago."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Checkout iniciado exitosamente",
            content = @Content(schema = @Schema(implementation = CheckoutInitiateResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error en la solicitud (carrito vacío, datos inválidos, etc.)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al procesar el checkout"
        )
    })
    public ResponseEntity<ApiResponse<CheckoutInitiateResponse>> initiateCheckout(
            @Valid @RequestBody CheckoutInitiateRequest request
    ) {
        try {
            log.info("Iniciando checkout para carrito: {}", request.getCarritoId());

            CheckoutInitiateResponse response = checkoutService.initiateCheckout(request);

            log.info("Checkout iniciado exitosamente - Pedido: {}, Token: {}",
                    response.getNumeroOrden(), response.getTransbankToken());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en checkout: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));

        } catch (RuntimeException e) {
            log.error("Error al iniciar checkout: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al procesar el checkout: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Confirma el pago después de que el usuario regresa de Transbank
     * Verifica el estado del pago y actualiza el pedido correspondiente
     *
     * @param token Token de Transbank recibido en la URL de retorno
     * @return Pedido confirmado con estado actualizado
     */
    @GetMapping("/confirm")
    @Operation(
        summary = "Confirmar pago",
        description = "Confirma el pago con Transbank usando el token recibido. " +
                     "Este endpoint es llamado cuando el usuario regresa desde la página de pago de Transbank. " +
                     "Actualiza el estado del pedido según el resultado del pago."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pago confirmado exitosamente",
            content = @Content(schema = @Schema(implementation = PedidoResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Token inválido o pago rechazado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pago no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al confirmar el pago"
        )
    })
    public ResponseEntity<ApiResponse<PedidoResponseDto>> confirmCheckout(
            @Parameter(description = "Token de Transbank", required = true)
            @RequestParam(name = "token_ws") String token
    ) {
        try {
            log.info("Confirmando checkout con token: {}", token);

            Pedido pedido = checkoutService.confirmCheckout(token);
            PedidoResponseDto response = PedidoResponseDto.fromEntity(pedido);

            log.info("Checkout confirmado - Pedido: {}, Estado: {}",
                    pedido.getNumeroPedido(), pedido.getEstadoPedido());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Error de validación en confirmación: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));

        } catch (RuntimeException e) {
            log.error("Error al confirmar checkout: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al confirmar el pago: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Endpoint alternativo para confirmación con método POST
     * Algunos sistemas de Transbank pueden usar POST en lugar de GET
     */
    @PostMapping("/confirm")
    @Operation(
        summary = "Confirmar pago (POST)",
        description = "Endpoint alternativo para confirmación de pago usando método POST. " +
                     "Funcionalmente idéntico al endpoint GET."
    )
    public ResponseEntity<ApiResponse<PedidoResponseDto>> confirmCheckoutPost(
            @Parameter(description = "Token de Transbank", required = true)
            @RequestParam(name = "token_ws") String token
    ) {
        return confirmCheckout(token);
    }
}
