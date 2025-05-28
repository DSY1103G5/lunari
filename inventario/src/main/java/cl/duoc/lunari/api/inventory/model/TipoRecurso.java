package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "tiporecurso")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TipoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_recurso")
    private Integer idTipoRecurso;

    @Column(name = "nombre_tipo_recurso", unique = true, nullable = false, length = 100)
    private String nombreTipoRecurso;

    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida;

    @Column(name = "tarifa_base_por_hora", precision = 10, scale = 2)
    private BigDecimal tarifaBasePorHora;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEl;

    @OneToMany(mappedBy = "tipoRecurso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<PaqueteRecursoServicio> paqueteRecursoServicios;

    @PrePersist
    protected void onCreate() {
        this.creadoEl = OffsetDateTime.now();
        this.actualizadoEl = OffsetDateTime.now();
        if (this.unidadMedida == null) {
            this.unidadMedida = "Hour";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEl = OffsetDateTime.now();
    }
}