package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.ServicioServicioAdicional;
import cl.duoc.lunari.api.inventory.model.ServicioServicioAdicional.ServicioServicioAdicionalId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioServicioAdicionalRepository extends JpaRepository<ServicioServicioAdicional, ServicioServicioAdicionalId> {
}
