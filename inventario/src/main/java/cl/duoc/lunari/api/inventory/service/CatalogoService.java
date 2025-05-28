package cl.duoc.lunari.api.inventory.service;

import cl.duoc.lunari.api.inventory.model.Catalogo;
import cl.duoc.lunari.api.inventory.repository.CatalogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogoService {

    @Autowired
    private CatalogoRepository catalogoRepository;

    public List<Catalogo> findAll() {
        return catalogoRepository.findAll();
    }

    public Optional<Catalogo> findById(Integer id) {
        return catalogoRepository.findById(id);
    }

    public Catalogo save(Catalogo catalogo) {
        return catalogoRepository.save(catalogo);
    }

    public void deleteById(Integer id) {
        catalogoRepository.deleteById(id);
    }

    public List<Catalogo> findByCategoria(Integer categoriaId) {
        return catalogoRepository.findByCategoriaIdCategoria(categoriaId);
    }

    public List<Catalogo> findByNombreContaining(String nombre) {
        return catalogoRepository.findByNombreServicioContainingIgnoreCase(nombre);
    }

    public Catalogo activar(Integer id) {
        Optional<Catalogo> catalogo = catalogoRepository.findById(id);
        if (catalogo.isPresent()) {
            catalogo.get().setIsActivo(true);
            return catalogoRepository.save(catalogo.get());
        }
        return null;
    }

    public Catalogo desactivar(Integer id) {
        Optional<Catalogo> catalogo = catalogoRepository.findById(id);
        if (catalogo.isPresent()) {
            catalogo.get().setIsActivo(false);
            return catalogoRepository.save(catalogo.get());
        }
        return null;
    }

    public List<Catalogo> findAllActivos() {
        return catalogoRepository.findByIsActivoTrue();
    }
}
