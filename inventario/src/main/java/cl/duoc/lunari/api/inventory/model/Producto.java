package cl.duoc.lunari.api.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "producto")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonIgnoreProperties("productos")
    private Categoria categoria;

    @Column(name = "precio_clp", nullable = false)
    private Integer precioCLP;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Type(JsonBinaryType.class)
    @Column(name = "specs", columnDefinition = "jsonb")
    private List<String> specs;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Type(JsonBinaryType.class)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    @Column(name = "imagen", length = 500)
    private String imagen;

    @Column(name = "is_activo")
    private Boolean isActivo = true;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEl;

    @PrePersist
    protected void onCreate() {
        this.creadoEl = OffsetDateTime.now();
        this.actualizadoEl = OffsetDateTime.now();
        if (this.isActivo == null) {
            this.isActivo = true;
        }
        if (this.stock == null) {
            this.stock = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEl = OffsetDateTime.now();
    }
}
