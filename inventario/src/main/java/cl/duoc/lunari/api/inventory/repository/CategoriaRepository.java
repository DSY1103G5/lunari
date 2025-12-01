package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.Categoria;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, String> {
    Optional<Categoria> findByNombreCategoria(String nombre);
    List<Categoria> findByNombreCategoriaContainingIgnoreCase(String nombre);
}