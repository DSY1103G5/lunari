package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.TipoRecurso;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRecursoRepository extends JpaRepository<TipoRecurso, Integer> {
    Optional<TipoRecurso> findByNombreTipoRecurso(String nombreTipoRecurso);
    List<TipoRecurso> findByNombreTipoRecursoContainingIgnoreCase(String nombreTipoRecurso);
    List<TipoRecurso> findByUnidadMedida(String unidadMedida);
}
