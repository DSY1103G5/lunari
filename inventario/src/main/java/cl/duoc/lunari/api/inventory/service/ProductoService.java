package cl.duoc.lunari.api.inventory.service;

import cl.duoc.lunari.api.inventory.model.Categoria;
import cl.duoc.lunari.api.inventory.model.Producto;
import cl.duoc.lunari.api.inventory.repository.CategoriaRepository;
import cl.duoc.lunari.api.inventory.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(Integer id) {
        return productoRepository.findById(id);
    }

    public Optional<Producto> findByCode(String code) {
        return productoRepository.findByCode(code);
    }

    public Producto save(Producto producto) {
        // Validate category exists
        if (producto.getCategoria() != null && producto.getCategoria().getIdCategoria() != null) {
            Optional<Categoria> categoria = categoriaRepository.findById(producto.getCategoria().getIdCategoria());
            if (categoria.isEmpty()) {
                throw new RuntimeException("Categoría no encontrada con ID: " + producto.getCategoria().getIdCategoria());
            }
            producto.setCategoria(categoria.get());
        }

        // Validate code uniqueness for new products
        if (producto.getIdProducto() == null && producto.getCode() != null) {
            Optional<Producto> existing = productoRepository.findByCode(producto.getCode());
            if (existing.isPresent()) {
                throw new RuntimeException("Ya existe un producto con el código: " + producto.getCode());
            }
        }

        return productoRepository.save(producto);
    }

    public void deleteById(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    public List<Producto> findByCategoria(String categoriaId) {
        return productoRepository.findByCategoriaIdCategoria(categoriaId);
    }

    public List<Producto> findByNombreContaining(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<Producto> findByMarca(String marca) {
        return productoRepository.findByMarca(marca);
    }

    public List<Producto> findActivos() {
        return productoRepository.findByIsActivoTrue();
    }

    public List<Producto> findEnStock() {
        return productoRepository.findByStockGreaterThan(0);
    }

    public List<Producto> findByPrecioRange(Integer min, Integer max) {
        return productoRepository.findByPrecioCLPBetween(min, max);
    }

    public List<Producto> findByMinRating(BigDecimal minRating) {
        return productoRepository.findByRatingGreaterThanEqual(minRating);
    }

    public List<Producto> findByTag(String tag) {
        // Format tag as JSON array element for JSONB query
        String jsonTag = "\"" + tag + "\"";
        return productoRepository.findByTag(jsonTag);
    }

    public Producto activar(Integer id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        Producto p = producto.get();
        p.setIsActivo(true);
        return productoRepository.save(p);
    }

    public Producto desactivar(Integer id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        Producto p = producto.get();
        p.setIsActivo(false);
        return productoRepository.save(p);
    }

    public Producto actualizarStock(Integer id, Integer nuevoStock) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        Producto p = producto.get();
        p.setStock(nuevoStock);
        return productoRepository.save(p);
    }
}
