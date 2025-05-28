package cl.duoc.lunari.api.cart.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceClient {

    private final RestTemplate restTemplate;

    @Value("${lunari.services.inventario.url:http://localhost:8082}")
    private String inventarioServiceUrl;

    /**
     * Obtiene información de un servicio
     */
    public ServicioInfo obtenerServicio(Integer servicioId) {
        try {
            log.debug("Obteniendo información del servicio: {}", servicioId);
            
            String url = inventarioServiceUrl + "/api/servicios/" + servicioId;
            
            ServicioInfo servicio = restTemplate.getForObject(url, ServicioInfo.class);
            
            if (servicio == null) {
                throw new RuntimeException("Servicio no encontrado: " + servicioId);
            }
            
            log.debug("Servicio obtenido: {}", servicioId);
            return servicio;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Servicio no encontrado: {}", servicioId);
            return null;
            
        } catch (Exception e) {
            log.error("Error al obtener servicio {}: {}", servicioId, e.getMessage());
            throw new RuntimeException("Error de comunicación con servicio de inventario", e);
        }
    }

    /**
     * Obtiene información de un servicio adicional
     */
    public ServicioAdicionalInfo obtenerServicioAdicional(Integer servicioAdicionalId) {
        try {
            log.debug("Obteniendo información del servicio adicional: {}", servicioAdicionalId);
            
            String url = inventarioServiceUrl + "/api/servicios-adicionales/" + servicioAdicionalId;
            
            ServicioAdicionalInfo servicioAdicional = restTemplate.getForObject(url, ServicioAdicionalInfo.class);
            
            if (servicioAdicional == null) {
                throw new RuntimeException("Servicio adicional no encontrado: " + servicioAdicionalId);
            }
            
            log.debug("Servicio adicional obtenido: {}", servicioAdicionalId);
            return servicioAdicional;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Servicio adicional no encontrado: {}", servicioAdicionalId);
            return null;
            
        } catch (Exception e) {
            log.error("Error al obtener servicio adicional {}: {}", servicioAdicionalId, e.getMessage());
            throw new RuntimeException("Error de comunicación con servicio de inventario", e);
        }
    }

    /**
     * Verifica si un servicio está disponible
     */
    public boolean servicioEstaDisponible(Integer servicioId) {
        try {
            ServicioInfo servicio = obtenerServicio(servicioId);
            return servicio != null && servicio.isDisponible();
            
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad del servicio {}: {}", servicioId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un servicio adicional está disponible
     */
    public boolean servicioAdicionalEstaDisponible(Integer servicioAdicionalId) {
        try {
            ServicioAdicionalInfo servicioAdicional = obtenerServicioAdicional(servicioAdicionalId);
            return servicioAdicional != null && servicioAdicional.isDisponible();
            
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad del servicio adicional {}: {}", 
                     servicioAdicionalId, e.getMessage());
            return false;
        }
    }

    /**
     * DTO para información del servicio
     */
    public static class ServicioInfo {
        private Integer id;
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
        private String categoria;
        private boolean disponible;

        // Constructors
        public ServicioInfo() {}

        public ServicioInfo(Integer id, String nombre, String descripcion, BigDecimal precio, 
                          String categoria, boolean disponible) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.categoria = categoria;
            this.disponible = disponible;
        }

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
        }

        public String getCategoria() {
            return categoria;
        }

        public void setCategoria(String categoria) {
            this.categoria = categoria;
        }

        public boolean isDisponible() {
            return disponible;
        }

        public void setDisponible(boolean disponible) {
            this.disponible = disponible;
        }
    }

    /**
     * DTO para información del servicio adicional
     */
    public static class ServicioAdicionalInfo {
        private Integer id;
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
        private boolean disponible;

        // Constructors
        public ServicioAdicionalInfo() {}

        public ServicioAdicionalInfo(Integer id, String nombre, String descripcion, 
                                   BigDecimal precio, boolean disponible) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.disponible = disponible;
        }

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public BigDecimal getPrecio() {
            return precio;
        }

        public void setPrecio(BigDecimal precio) {
            this.precio = precio;
        }

        public boolean isDisponible() {
            return disponible;
        }

        public void setDisponible(boolean disponible) {
            this.disponible = disponible;
        }
    }
}