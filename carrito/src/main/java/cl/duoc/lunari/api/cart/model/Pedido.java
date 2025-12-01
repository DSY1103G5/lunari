package cl.duoc.lunari.api.cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad Pedido (Order)
 * Representa un pedido generado a partir de un carrito de compras
 */
@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue
    @Column(name = "id_pedido")
    private UUID id;

    @Column(name = "numero_pedido", nullable = false, unique = true, length = 50)
    @NotNull(message = "Número de pedido no puede estar vacío")
    @Size(max = 50, message = "Número de pedido no puede exceder 50 caracteres")
    private String numeroPedido;

    @Column(name = "id_carrito", nullable = false)
    @NotNull(message = "ID de carrito no puede estar vacío")
    private UUID carritoId;

    @Column(name = "id_usuario_ext", nullable = false)
    @NotNull(message = "ID de usuario no puede estar vacío")
    private UUID usuarioId;

    @Column(name = "estado_pedido", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Estado de pedido no puede estar vacío")
    private EstadoPedido estadoPedido = EstadoPedido.CREADO;

    @Column(name = "total_productos", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Total de productos no puede estar vacío")
    @DecimalMin(value = "0.0", message = "Total de productos debe ser mayor o igual a 0")
    private BigDecimal totalProductos = BigDecimal.ZERO;

    @Column(name = "total_puntos_ganados")
    @Min(value = 0, message = "Total de puntos ganados debe ser mayor o igual a 0")
    private Integer totalPuntosGanados = 0;

    @Column(name = "notas_cliente", columnDefinition = "TEXT")
    private String notasCliente;

    @Column(name = "creado_el", nullable = false, updatable = false)
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", nullable = false)
    private OffsetDateTime actualizadoEl;

    @Column(name = "completado_el")
    private OffsetDateTime completadoEl;

    // Relación uno a muchos con PedidoItem
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PedidoItem> items = new ArrayList<>();

    // Relación uno a uno con Pago
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Pago pago;

    @PrePersist
    protected void onCreate() {
        creadoEl = OffsetDateTime.now();
        actualizadoEl = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEl = OffsetDateTime.now();
    }

    /**
     * Marca el pedido como completado
     */
    public void marcarComoCompletado() {
        this.estadoPedido = EstadoPedido.COMPLETADO;
        this.completadoEl = OffsetDateTime.now();
    }

    /**
     * Calcula los puntos a ganar basado en el total
     * Regla: 1 punto por cada 100 CLP
     */
    public Integer calcularPuntosAGanar() {
        if (totalProductos == null) {
            return 0;
        }
        return totalProductos.divide(new BigDecimal("100"), 0, BigDecimal.ROUND_DOWN).intValue();
    }

    /**
     * Agrega un item al pedido
     */
    public void agregarItem(PedidoItem item) {
        items.add(item);
        item.setPedido(this);
    }

    /**
     * Establece el pago para este pedido
     */
    public void setPago(Pago pago) {
        this.pago = pago;
        if (pago != null) {
            pago.setPedido(this);
        }
    }
}
