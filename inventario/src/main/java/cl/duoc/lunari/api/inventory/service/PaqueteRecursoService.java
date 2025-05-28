package cl.duoc.lunari.api.inventory.service;

import cl.duoc.lunari.api.inventory.model.PaqueteRecursoServicio;
import cl.duoc.lunari.api.inventory.repository.PaqueteRecursoServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PaqueteRecursoService {

    @Autowired
    private PaqueteRecursoServicioRepository paqueteRecursoRepository;

    public List<PaqueteRecursoServicio> findAll() {
        return paqueteRecursoRepository.findAll();
    }

    public Optional<PaqueteRecursoServicio> findById(Integer id) {
        return paqueteRecursoRepository.findById(id);
    }

    public PaqueteRecursoServicio save(PaqueteRecursoServicio paqueteRecurso) {
        return paqueteRecursoRepository.save(paqueteRecurso);
    }

    public void deleteById(Integer id) {
        paqueteRecursoRepository.deleteById(id);
    }

    public List<PaqueteRecursoServicio> findByServicio(Integer servicioId) {
        return paqueteRecursoRepository.findByServicioIdServicio(servicioId);
    }

    public List<PaqueteRecursoServicio> findByTipoRecurso(Integer tipoRecursoId) {
        return paqueteRecursoRepository.findByTipoRecursoIdTipoRecurso(tipoRecursoId);
    }

    public BigDecimal calcularCostoRecursos(Integer servicioId) {
        List<PaqueteRecursoServicio> recursos = findByServicio(servicioId);
        return recursos.stream()
                .map(this::calcularCostoRecurso)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularCostoRecurso(PaqueteRecursoServicio paquete) {
        BigDecimal tarifa = paquete.getTipoRecurso().getTarifaBasePorHora();
        BigDecimal cantidad = BigDecimal.valueOf(paquete.getCantidadEstimado());
        return tarifa.multiply(cantidad);
    }
}
