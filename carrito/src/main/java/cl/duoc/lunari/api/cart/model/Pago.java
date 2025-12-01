package cl.duoc.lunari.api.cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad Pago (Payment)
 * Representa un pago asociado a un pedido
 * Almacena detalles de la transacción con Transbank WebPay Plus
 */
@Entity
@Table(name = "pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue
    @Column(name = "id_pago")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    @NotNull(message = "Pedido no puede estar vacío")
    private Pedido pedido;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Método de pago no puede estar vacío")
    private MetodoPago metodoPago = MetodoPago.WEBPAY_PLUS;

    @Column(name = "estado_pago", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Estado de pago no puede estar vacío")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Monto total no puede estar vacío")
    @DecimalMin(value = "0.0", message = "Monto total debe ser mayor o igual a 0")
    private BigDecimal montoTotal;

    // Campos específicos de Transbank WebPay Plus
    @Column(name = "transbank_token", length = 100)
    @Size(max = 100, message = "Token de Transbank no puede exceder 100 caracteres")
    private String transbankToken;

    @Column(name = "transbank_buy_order", nullable = false, length = 100)
    @NotNull(message = "Buy order de Transbank no puede estar vacío")
    @Size(max = 100, message = "Buy order de Transbank no puede exceder 100 caracteres")
    private String transbankBuyOrder;

    @Column(name = "transbank_session_id", nullable = false, length = 100)
    @NotNull(message = "Session ID de Transbank no puede estar vacío")
    @Size(max = 100, message = "Session ID de Transbank no puede exceder 100 caracteres")
    private String transbankSessionId;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "authorization_code", length = 50)
    @Size(max = 50, message = "Código de autorización no puede exceder 50 caracteres")
    private String authorizationCode;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "creado_el", nullable = false, updatable = false)
    private OffsetDateTime creadoEl;

    @Column(name = "confirmado_el")
    private OffsetDateTime confirmadoEl;

    @PrePersist
    protected void onCreate() {
        creadoEl = OffsetDateTime.now();
    }

    /**
     * Marca el pago como aprobado
     */
    public void marcarComoAprobado(String authCode, Integer respCode) {
        this.estadoPago = EstadoPago.APROBADO;
        this.authorizationCode = authCode;
        this.responseCode = respCode;
        this.confirmadoEl = OffsetDateTime.now();
    }

    /**
     * Marca el pago como rechazado
     */
    public void marcarComoRechazado(Integer respCode) {
        this.estadoPago = EstadoPago.RECHAZADO;
        this.responseCode = respCode;
        this.confirmadoEl = OffsetDateTime.now();
    }

    /**
     * Verifica si el pago está aprobado
     */
    public boolean estaAprobado() {
        return estadoPago == EstadoPago.APROBADO;
    }

    /**
     * Verifica si el pago está pendiente
     */
    public boolean estaPendiente() {
        return estadoPago == EstadoPago.PENDIENTE;
    }
}
