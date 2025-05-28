package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Table(name = "paqueterecursoservicio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaqueteRecursoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete_recurso")
    private Integer idPaqueteRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", referencedColumnName = "id_servicio")
    private Catalogo servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_recurso", referencedColumnName = "id_tipo_recurso")
    private TipoRecurso tipoRecurso;

    @Column(name = "cantidad_recurso", nullable = false)
    private Integer cantidadRecurso;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEl;

    @PrePersist
    protected void onCreate() {
        this.creadoEl = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEl = OffsetDateTime.now();
    }
}
