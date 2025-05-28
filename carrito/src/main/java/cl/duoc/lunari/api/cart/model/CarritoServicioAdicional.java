package cl.duoc.lunari.api.cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "Carrito_Servicio_Adicional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoServicioAdicional {

    @Id
    @GeneratedValue
    @Column(name = "id_carrito_servicio_adicional")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito_item", nullable = false)
    @NotNull(message = "Carrito item no puede estar vacío")
    private CarritoItem carritoItem;

    @Column(name = "id_servicio_adicional_ext", nullable = false)
    @NotNull(message = "ID de servicio adicional no puede estar vacío")
    private Integer servicioAdicionalId;

    @Column(name = "precio_adicional", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Precio adicional debe ser mayor o igual a 0")
    private BigDecimal precioAdicional;

    @Column(name = "creado_el", updatable = false)
    private OffsetDateTime creadoEl;

    @PrePersist
    protected void onCreate() {
        creadoEl = OffsetDateTime.now();
    }
}