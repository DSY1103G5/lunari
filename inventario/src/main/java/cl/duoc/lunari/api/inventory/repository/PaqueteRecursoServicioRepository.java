package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.PaqueteRecursoServicio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaqueteRecursoServicioRepository extends JpaRepository<PaqueteRecursoServicio, Integer> {
    List<PaqueteRecursoServicio> findByServicioIdServicio(Integer servicioId);
    List<PaqueteRecursoServicio> findByTipoRecursoIdTipoRecurso(Integer tipoRecursoId);
}