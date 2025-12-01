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
import java.util.UUID;

/**
 * Entidad PedidoItem (Order Line Item)
 * Representa un producto dentro de un pedido
 * Almacena una "foto" (snapshot) del producto al momento de la compra
 */
@Entity
@Table(name = "pedido_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoItem {

    @Id
    @GeneratedValue
    @Column(name = "id_pedido_item")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    @NotNull(message = "Pedido no puede estar vacío")
    private Pedido pedido;

    @Column(name = "id_producto_ext", nullable = false)
    @NotNull(message = "ID de producto no puede estar vacío")
    private Long productoId;

    @Column(name = "codigo_producto", nullable = false, length = 20)
    @NotNull(message = "Código de producto no puede estar vacío")
    @Size(max = 20, message = "Código de producto no puede exceder 20 caracteres")
    private String codigoProducto;

    @Column(name = "nombre_producto", nullable = false)
    @NotNull(message = "Nombre de producto no puede estar vacío")
    @Size(max = 255, message = "Nombre de producto no puede exceder 255 caracteres")
    private String nombreProducto;

    @Column(name = "cantidad", nullable = false)
    @NotNull(message = "Cantidad no puede estar vacía")
    @Min(value = 1, message = "Cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Precio unitario no puede estar vacío")
    @DecimalMin(value = "0.0", message = "Precio unitario debe ser mayor o igual a 0")
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Subtotal no puede estar vacío")
    @DecimalMin(value = "0.0", message = "Subtotal debe ser mayor o igual a 0")
    private BigDecimal subtotal;

    @Column(name = "creado_el", nullable = false, updatable = false)
    private OffsetDateTime creadoEl;

    @PrePersist
    protected void onCreate() {
        creadoEl = OffsetDateTime.now();
        // Calcular subtotal si no está establecido
        if (subtotal == null && precioUnitario != null && cantidad != null) {
            calcularSubtotal();
        }
    }

    /**
     * Calcula el subtotal basado en precio unitario y cantidad
     */
    public void calcularSubtotal() {
        if (precioUnitario != null && cantidad != null) {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}
