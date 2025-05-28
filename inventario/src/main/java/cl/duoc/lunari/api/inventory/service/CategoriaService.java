package cl.duoc.lunari.api.inventory.service;

import cl.duoc.lunari.api.inventory.model.Categoria;
import cl.duoc.lunari.api.inventory.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> findById(Integer id) {
        return categoriaRepository.findById(id);
    }

    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public void deleteById(Integer id) {
        categoriaRepository.deleteById(id);
    }

    public List<Categoria> findByNombreContaining(String nombre) {
        return categoriaRepository.findByNombreCategoriaContainingIgnoreCase(nombre);
    }

    public Optional<Categoria> findByNombre(String nombre) {
        return categoriaRepository.findByNombreCategoria(nombre);
    }
}
