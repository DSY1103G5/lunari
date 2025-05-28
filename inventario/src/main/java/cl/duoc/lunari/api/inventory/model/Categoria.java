package cl.duoc.lunari.api.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "categoria")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;

    @Column(name = "nombre_categoria", unique = true, nullable = false, length = 100)
    private String nombreCategoria;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "creado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEl;

    @Column(name = "actualizado_el", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEl;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Catalogo> servicios;

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