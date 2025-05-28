package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;

/**
 * Junction table for many-to-many relationship between services and additional services.
 * Maps to the 'ServicioServicioAdicional' table.
 */
@Entity
@Table(name = "servicioservicioadicional")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServicioServicioAdicional {

    @EmbeddedId
    private ServicioServicioAdicionalId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idServicio")
    @JoinColumn(name = "id_servicio")
    @JsonIgnore
    private Catalogo servicio;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idServicioAdicional")
    @JoinColumn(name = "id_servicio_adicional")
    private ServicioAdicional servicioAdicional;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @PrePersist
    protected void onCreate() {
        this.creadoEl = OffsetDateTime.now();
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServicioServicioAdicionalId implements java.io.Serializable {
        @Column(name = "id_servicio")
        private Integer idServicio;

        @Column(name = "id_servicio_adicional") 
        private Integer idServicioAdicional;
    }
}