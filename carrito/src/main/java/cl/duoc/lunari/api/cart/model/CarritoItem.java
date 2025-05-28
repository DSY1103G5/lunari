package cl.duoc.lunari.api.cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Carrito_Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    @Id
    @GeneratedValue
    @Column(name = "id_carrito_item")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito", nullable = false)
    @NotNull(message = "Carrito no puede estar vacío")
    private Carrito carrito;

    @Column(name = "id_servicio_ext", nullable = false)
    @NotNull(message = "ID de servicio no puede estar vacío")
    private Integer servicioId;

    @Column(name = "cantidad", nullable = false)
    @Min(value = 1, message = "Cantidad debe ser mayor a 0")
    private Integer cantidad = 1;    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Precio unitario debe ser mayor o igual a 0")
    @NotNull(message = "Precio unitario no puede estar vacío")
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Subtotal debe ser mayor o igual a 0")
    private BigDecimal subtotal;

    @Column(name = "personalizaciones")
    private String personalizaciones;

    @Column(name = "creado_el", updatable = false)
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el")
    private OffsetDateTime actualizadoEl;

    @OneToMany(mappedBy = "carritoItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarritoServicioAdicional> serviciosAdicionales;

    @PrePersist
    protected void onCreate() {
        creadoEl = OffsetDateTime.now();
        actualizadoEl = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEl = OffsetDateTime.now();
    }
}