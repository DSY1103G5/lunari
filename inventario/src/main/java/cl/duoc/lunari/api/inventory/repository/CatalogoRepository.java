package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.Catalogo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogoRepository extends JpaRepository<Catalogo, Integer> {
    List<Catalogo> findByCategoriaIdCategoria(Integer categoriaId);
    List<Catalogo> findByNombreServicioContainingIgnoreCase(String nombre);
    List<Catalogo> findByIsActivoTrue();
}
