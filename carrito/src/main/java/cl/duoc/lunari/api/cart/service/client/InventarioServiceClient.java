package cl.duoc.lunari.api.cart.service.client;

import cl.duoc.lunari.api.cart.dto.StockReductionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceClient {

    private final RestTemplate restTemplate;

    @Value("${lunari.services.inventario.url:http://localhost:8082}")
    private String inventarioServiceUrl;    /**
     * Obtiene información de un servicio
     */
    public ServicioInfo obtenerServicio(Integer servicioId) {
        log.debug("Obteniendo información del servicio: {}", servicioId);
        try {
            
            String url = inventarioServiceUrl + "/api/v1/inventory/catalogo/" + servicioId;

            ResponseEntity<ApiResponse<ServicioInfo>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<ApiResponse<ServicioInfo>>() {}
            );

            ApiResponse<ServicioInfo> apiResponse = response.getBody();
            
            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getResponse() == null) {
                throw new RuntimeException("Servicio no encontrado: " + servicioId);
            }
            
            log.debug("Servicio obtenido: {}", servicioId);
            return apiResponse.getResponse();
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Servicio no encontrado: {}", servicioId);
            return null;
            
        } catch (Exception e) {
            log.warn("Error al obtener servicio {}: {}", servicioId, e.getMessage());
            throw new RuntimeException("Error de comunicación con servicio de inventario", e);
        }
    }/**
     * Obtiene información de un servicio adicional
     */
    public ServicioAdicionalInfo obtenerServicioAdicional(Integer servicioAdicionalId) {
        try {
            log.debug("Obteniendo información del servicio adicional: {}", servicioAdicionalId);
            
            String url = inventarioServiceUrl + "/api/v1/inventory/servicios-adicionales/" + servicioAdicionalId;
            
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
            log.warn("Error al obtener servicio adicional {}: {}", servicioAdicionalId, e.getMessage());
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
     * Reduce el stock de productos en el servicio de inventario
     * Este método utiliza best-effort: si falla, registra el error pero no lanza excepción
     *
     * @param reductions Lista de reducciones de stock a aplicar
     * @return true si la reducción fue exitosa, false si falló
     */
    public boolean reduceStock(List<StockReductionRequest.StockItem> reductions) {
        try {
            log.info("Reduciendo stock para {} productos", reductions.size());

            String url = inventarioServiceUrl + "/api/v1/inventory/stock/reduce";

            StockReductionRequest request = new StockReductionRequest();
            request.setItems(reductions);

            HttpEntity<StockReductionRequest> requestEntity = new HttpEntity<>(request);

            ResponseEntity<ApiResponse<StockReductionResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<StockReductionResponse>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Stock reducido exitosamente");
                return true;
            }

            log.warn("La reducción de stock no fue exitosa: {}", response.getStatusCode());
            return false;

        } catch (Exception e) {
            log.error("Error al reducir stock en servicio de inventario: {}", e.getMessage(), e);
            // Best-effort: no lanzamos excepción, solo registramos el error
            return false;
        }
    }

    /**
     * Verifica si hay stock suficiente para los productos especificados
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad requerida
     * @return true si hay stock suficiente, false en caso contrario
     */
    public boolean checkStock(Long productoId, Integer cantidad) {
        try {
            log.debug("Verificando stock para producto {}, cantidad: {}", productoId, cantidad);

            String url = inventarioServiceUrl + "/api/v1/inventory/stock/check/" + productoId + "/" + cantidad;

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getResponse() != null && response.getBody().getResponse();
            }

            return false;

        } catch (Exception e) {
            log.warn("Error al verificar stock para producto {}: {}", productoId, e.getMessage());
            // En caso de error, asumimos que no hay stock para evitar sobreventa
            return false;
        }
    }

    /**
     * DTO para información del servicio
     */
    public static class ServicioInfo {
        private Integer idServicio;
        private String nombreServicio;
        private String descripcion;
        private BigDecimal precioBase;
        private CategoriaInfo categoria;
        private boolean isActivo;
        private Integer duracionEstimadaDias;

        // Constructors
        public ServicioInfo() {}

        public ServicioInfo(Integer idServicio, String nombreServicio, String descripcion, BigDecimal precioBase, 
                          CategoriaInfo categoria, boolean isActivo) {
            this.idServicio = idServicio;
            this.nombreServicio = nombreServicio;
            this.descripcion = descripcion;
            this.precioBase = precioBase;
            this.categoria = categoria;
            this.isActivo = isActivo;
        }// Getters and Setters
        public Integer getIdServicio() {
            return idServicio;
        }

        public void setIdServicio(Integer idServicio) {
            this.idServicio = idServicio;
        }

        public String getNombreServicio() {
            return nombreServicio;
        }

        public void setNombreServicio(String nombreServicio) {
            this.nombreServicio = nombreServicio;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public BigDecimal getPrecioBase() {
            return precioBase;
        }

        public void setPrecioBase(BigDecimal precioBase) {
            this.precioBase = precioBase;
        }        public CategoriaInfo getCategoria() {
            return categoria;
        }

        public void setCategoria(CategoriaInfo categoria) {
            this.categoria = categoria;
        }

        public boolean isDisponible() {
            return isActivo;
        }

        public void setDisponible(boolean disponible) {
            this.isActivo = disponible;
        }

        public boolean isActivo() {
            return isActivo;
        }        public void setActivo(boolean activo) {
            this.isActivo = activo;
        }

        public Integer getDuracionEstimadaDias() {
            return duracionEstimadaDias;
        }

        public void setDuracionEstimadaDias(Integer duracionEstimadaDias) {
            this.duracionEstimadaDias = duracionEstimadaDias;
        }
    }    /**
     * DTO para información del servicio adicional
     */
    public static class ServicioAdicionalInfo {
        private Integer idServicioAdicional;
        private String nombreAdicional;
        private String descripcion;
        private BigDecimal precioAdicional;
        private boolean isActivo;

        // Constructors
        public ServicioAdicionalInfo() {}

        public ServicioAdicionalInfo(Integer idServicioAdicional, String nombreAdicional, String descripcion, 
                                   BigDecimal precioAdicional, boolean isActivo) {
            this.idServicioAdicional = idServicioAdicional;
            this.nombreAdicional = nombreAdicional;
            this.descripcion = descripcion;
            this.precioAdicional = precioAdicional;
            this.isActivo = isActivo;
        }

        // Getters and Setters
        public Integer getIdServicioAdicional() {
            return idServicioAdicional;
        }

        public void setIdServicioAdicional(Integer idServicioAdicional) {
            this.idServicioAdicional = idServicioAdicional;
        }

        public String getNombreAdicional() {
            return nombreAdicional;
        }

        public void setNombreAdicional(String nombreAdicional) {
            this.nombreAdicional = nombreAdicional;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public BigDecimal getPrecioAdicional() {
            return precioAdicional;
        }

        public void setPrecioAdicional(BigDecimal precioAdicional) {
            this.precioAdicional = precioAdicional;
        }

        public boolean isDisponible() {
            return isActivo;
        }

        public void setDisponible(boolean disponible) {
            this.isActivo = disponible;
        }

        public boolean isActivo() {
            return isActivo;
        }

        public void setActivo(boolean activo) {
            this.isActivo = activo;
        }
    }    /**
     * DTO para información de categoría
     */
    public static class CategoriaInfo {
        private Integer idCategoria;
        private String nombreCategoria;
        private String descripcion;

        // Constructors
        public CategoriaInfo() {}

        // Getters and Setters
        public Integer getIdCategoria() {
            return idCategoria;
        }

        public void setIdCategoria(Integer idCategoria) {
            this.idCategoria = idCategoria;
        }

        public String getNombreCategoria() {
            return nombreCategoria;
        }

        public void setNombreCategoria(String nombreCategoria) {
            this.nombreCategoria = nombreCategoria;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }
    }    /**
     * Wrapper para las respuestas de la API
     */
    public static class ApiResponse<T> {
        private boolean success;
        private T response;
        private String message;
        private int statusCode;

        // Constructors
        public ApiResponse() {}

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public T getResponse() {
            return response;
        }

        public void setResponse(T response) {
            this.response = response;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
    }

    /**
     * DTO para la respuesta de reducción de stock
     */
    public static class StockReductionResponse {
        private int itemsProcessed;
        private String message;

        public StockReductionResponse() {}

        public int getItemsProcessed() {
            return itemsProcessed;
        }

        public void setItemsProcessed(int itemsProcessed) {
            this.itemsProcessed = itemsProcessed;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}