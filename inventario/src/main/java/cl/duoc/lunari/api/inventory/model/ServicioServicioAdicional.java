package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

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
    private Catalogo servicio;

    @ManyToOne(fetch = FetchType.LAZY)
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
    public static class ServicioServicioAdicionalId implements Serializable {

        @Column(name = "id_servicio")
        private Integer idServicio;

        @Column(name = "id_servicio_adicional")
        private Integer idServicioAdicional;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServicioServicioAdicionalId that = (ServicioServicioAdicionalId) o;
            return Objects.equals(idServicio, that.idServicio) &&
                   Objects.equals(idServicioAdicional, that.idServicioAdicional);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idServicio, idServicioAdicional);
        }
    }
}