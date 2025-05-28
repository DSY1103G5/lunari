package cl.duoc.lunari.api.inventory.service;

import cl.duoc.lunari.api.inventory.model.TipoRecurso;
import cl.duoc.lunari.api.inventory.repository.TipoRecursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoRecursoService {

    @Autowired
    private TipoRecursoRepository tipoRecursoRepository;

    public List<TipoRecurso> findAll() {
        return tipoRecursoRepository.findAll();
    }

    public Optional<TipoRecurso> findById(Integer id) {
        return tipoRecursoRepository.findById(id);
    }

    public TipoRecurso save(TipoRecurso tipoRecurso) {
        return tipoRecursoRepository.save(tipoRecurso);
    }

    public void deleteById(Integer id) {
        tipoRecursoRepository.deleteById(id);
    }

    public List<TipoRecurso> findByNombreContaining(String nombre) {
        return tipoRecursoRepository.findByNombreTipoRecursoContainingIgnoreCase(nombre);
    }

    public Optional<TipoRecurso> findByNombre(String nombre) {
        return tipoRecursoRepository.findByNombreTipoRecurso(nombre);
    }

    public List<TipoRecurso> findByUnidadMedida(String unidadMedida) {
        return tipoRecursoRepository.findByUnidadMedida(unidadMedida);
    }
}
