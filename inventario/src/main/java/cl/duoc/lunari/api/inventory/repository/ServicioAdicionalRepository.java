package cl.duoc.lunari.api.inventory.repository;

import cl.duoc.lunari.api.inventory.model.ServicioAdicional;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioAdicionalRepository extends JpaRepository<ServicioAdicional, Integer> {
    Optional<ServicioAdicional> findByNombreAdicional(String addonName);
    List<ServicioAdicional> findByIsActivoTrue();
}