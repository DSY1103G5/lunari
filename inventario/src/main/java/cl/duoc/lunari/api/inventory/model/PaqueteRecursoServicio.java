package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "paqueterecursoservicio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaqueteRecursoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete_recurso_servicio")
    private Integer idPaqueteRecursoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    @JsonIgnore
    private Catalogo servicio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_recurso", nullable = false)
    private TipoRecurso tipoRecurso;

    @Column(name = "cantidad_estimado", precision = 8, scale = 2, nullable = false)
    private BigDecimal cantidadEstimado;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEl;

    @PrePersist
    protected void onCreate() {
        this.creadoEl = OffsetDateTime.now();
        this.actualizadoEl = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEl = OffsetDateTime.now();
    }
}
