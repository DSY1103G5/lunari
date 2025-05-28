package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "catalogo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", foreignKey = @ForeignKey(name = "FK_SERVICE_CATEGORY"))
    private Categoria categoria;

    @Column(name = "nombre_servicio", unique = true, nullable = false, length = 255)
    private String nombreServicio;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio_base", precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "is_activo")
    private Boolean isActivo;

    @Column(name = "duracion_estimada_dias")
    private Integer duracionEstimadaDias;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEl;    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<PaqueteRecursoServicio> paqueteRecursoServicios;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<ServicioServicioAdicional> servicioServicioAdicionales;

    @PrePersist
    protected void onCreate() {
        this.creadoEl = OffsetDateTime.now();
        this.actualizadoEl = OffsetDateTime.now();
        if (this.isActivo == null) {
            this.isActivo = true; 
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEl = OffsetDateTime.now();
    }
    
    /**
     * Custom setter to handle categoria as either ID (number) or full object
     */
    @JsonSetter("categoria")
    public void setCategoriaFromJson(JsonNode categoriaNode) {
        if (categoriaNode.isNumber()) {
            // If it's a number, create a Categoria with just the ID
            Categoria cat = new Categoria();
            cat.setIdCategoria(categoriaNode.asInt());
            this.categoria = cat;
        } else if (categoriaNode.isObject()) {
            // If it's an object, handle it normally (Jackson will deserialize it)
            // This case is handled by the normal categoria setter
        }
    }
}