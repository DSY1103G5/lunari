package cl.duoc.lunari.api.inventory.service;

import cl.duoc.lunari.api.inventory.model.Catalogo;
import cl.duoc.lunari.api.inventory.model.Categoria;
import cl.duoc.lunari.api.inventory.repository.CatalogoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogoService {

    private final CatalogoRepository catalogoRepository;
    private final CategoriaService categoriaService;    public CatalogoService(CatalogoRepository catalogoRepository, CategoriaService categoriaService) {
        this.catalogoRepository = catalogoRepository;
        this.categoriaService = categoriaService;
    }

    public List<Catalogo> findAll() {
        return catalogoRepository.findAll();
    }

    public Optional<Catalogo> findById(Integer id) {
        return catalogoRepository.findById(id);
    }    public Catalogo save(Catalogo catalogo) {
        // If categoria only has ID, fetch the full categoria from database
        if (catalogo.getCategoria() != null && 
            catalogo.getCategoria().getIdCategoria() != null) {
            
            Optional<Categoria> categoriaCompleta = categoriaService.findById(catalogo.getCategoria().getIdCategoria());
            if (categoriaCompleta.isPresent()) {
                catalogo.setCategoria(categoriaCompleta.get());
            } else {
                throw new RuntimeException("Categoría con ID " + catalogo.getCategoria().getIdCategoria() + " no encontrada");
            }
        }
        
        return catalogoRepository.save(catalogo);
    }

    public void deleteCatalogo(Integer id) {
        if (!catalogoRepository.existsById(id)) {
            throw new RuntimeException("Servicio no encontrado : " + id);
        }
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
