package cl.duoc.lunari.api.cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Carrito")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carrito {

    @Id
    @GeneratedValue
    @Column(name = "id_carrito")
    private UUID id;

    @Column(name = "id_usuario_ext", nullable = false)
    @NotNull(message = "ID de usuario no puede estar vacío")
    private UUID usuarioId;

    @Column(name = "estado_carrito", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCarrito estado = EstadoCarrito.ACTIVO;

    @Column(name = "total_estimado", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Total debe ser mayor o igual a 0")
    private BigDecimal totalEstimado = BigDecimal.ZERO;

    @Column(name = "notas_cliente")
    private String notasCliente;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    @Column(name = "creado_el", updatable = false)
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el")
    private OffsetDateTime actualizadoEl;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarritoItem> items;

    @PrePersist
    protected void onCreate() {
        creadoEl = OffsetDateTime.now();
        actualizadoEl = OffsetDateTime.now();
        if (fechaExpiracion == null) {
            fechaExpiracion = OffsetDateTime.now().plusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEl = OffsetDateTime.now();
    }
}