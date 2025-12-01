package cl.duoc.lunari.api.cart.service.client;

import cl.duoc.lunari.api.cart.dto.AwardPointsRequest;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceClient {

    private final RestTemplate restTemplate;

    @Value("${lunari.services.usuario.url:http://localhost:8081}")
    private String usuarioServiceUrl;

    /**
     * Verifica si un usuario existe
     */
    public boolean existeUsuario(UUID usuarioId) {
        try {
            log.debug("Verificando existencia del usuario: {}", usuarioId);
            
            String url = usuarioServiceUrl + "/api/v1/users/" + usuarioId;
            
            // Hacer llamada HEAD para verificar existencia sin obtener el cuerpo
            restTemplate.headForHeaders(url);
            
            log.debug("Usuario encontrado: {}", usuarioId);
            return true;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Usuario no encontrado: {}", usuarioId);
            return false;
            
        } catch (Exception e) {
            log.error("Error al verificar usuario {}: {}", usuarioId, e.getMessage());
            throw new RuntimeException("Error de comunicación con servicio de usuarios", e);
        }
    }

    /**
     * Obtiene información básica de un usuario
     */
    public UsuarioInfo obtenerUsuario(UUID usuarioId) {
        try {
            log.debug("Obteniendo información del usuario: {}", usuarioId);
            
            String url = usuarioServiceUrl + "/api/v1/users/" + usuarioId;
            
            UsuarioInfo usuario = restTemplate.getForObject(url, UsuarioInfo.class);
            
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado: " + usuarioId);
            }
            
            log.debug("Usuario obtenido: {}", usuarioId);
            return usuario;
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Usuario no encontrado: {}", usuarioId);
            throw new RuntimeException("Usuario no encontrado: " + usuarioId);
            
        } catch (Exception e) {
            log.error("Error al obtener usuario {}: {}", usuarioId, e.getMessage());
            throw new RuntimeException("Error de comunicación con servicio de usuarios", e);
        }
    }

    /**
     * Verifica si un usuario está activo
     */
    public boolean usuarioEstaActivo(UUID usuarioId) {
        try {
            UsuarioInfo usuario = obtenerUsuario(usuarioId);
            return usuario != null && usuario.isActivo();

        } catch (Exception e) {
            log.error("Error al verificar estado del usuario {}: {}", usuarioId, e.getMessage());
            return false;
        }
    }

    /**
     * Otorga puntos a un usuario por una compra
     * Este método utiliza best-effort: si falla, registra el error pero no lanza excepción
     *
     * @param usuarioId ID del usuario
     * @param puntos Cantidad de puntos a otorgar
     * @param pedidoNumero Número del pedido que generó los puntos
     * @return true si los puntos fueron otorgados exitosamente, false si falló
     */
    public boolean awardPoints(UUID usuarioId, Integer puntos, String pedidoNumero) {
        try {
            log.info("Otorgando {} puntos al usuario {}", puntos, usuarioId);

            String url = usuarioServiceUrl + "/api/v1/users/" + usuarioId + "/points/award";

            AwardPointsRequest request = new AwardPointsRequest();
            request.setUsuarioId(usuarioId);
            request.setPuntos(puntos);
            request.setRazon("Compra - Pedido: " + pedidoNumero);
            request.setReferencia(pedidoNumero);

            HttpEntity<AwardPointsRequest> requestEntity = new HttpEntity<>(request);

            ResponseEntity<ApiResponse<PointsAwardResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<PointsAwardResponse>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Puntos otorgados exitosamente al usuario {}", usuarioId);
                return true;
            }

            log.warn("El otorgamiento de puntos no fue exitoso: {}", response.getStatusCode());
            return false;

        } catch (Exception e) {
            log.error("Error al otorgar puntos al usuario {}: {}", usuarioId, e.getMessage(), e);
            // Best-effort: no lanzamos excepción, solo registramos el error
            return false;
        }
    }

    /**
     * Obtiene el balance de puntos actual de un usuario
     *
     * @param usuarioId ID del usuario
     * @return Balance de puntos, o 0 si hay error
     */
    public Integer getPointsBalance(UUID usuarioId) {
        try {
            log.debug("Obteniendo balance de puntos del usuario: {}", usuarioId);

            String url = usuarioServiceUrl + "/api/v1/users/" + usuarioId + "/points/balance";

            ResponseEntity<ApiResponse<Integer>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponse<Integer>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                Integer balance = response.getBody().getResponse();
                return balance != null ? balance : 0;
            }

            return 0;

        } catch (Exception e) {
            log.warn("Error al obtener balance de puntos del usuario {}: {}", usuarioId, e.getMessage());
            return 0;
        }
    }

    /**
     * DTO para información básica del usuario
     */
    public static class UsuarioInfo {
        private UUID id;
        private String nombre;
        private String email;
        private boolean activo;

        // Constructors
        public UsuarioInfo() {}

        public UsuarioInfo(UUID id, String nombre, String email, boolean activo) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.activo = activo;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isActivo() {
            return activo;
        }

        public void setActivo(boolean activo) {
            this.activo = activo;
        }
    }

    /**
     * Wrapper para las respuestas de la API
     */
    public static class ApiResponse<T> {
        private boolean success;
        private T response;
        private String message;
        private int statusCode;

        public ApiResponse() {}

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
     * DTO para la respuesta de otorgamiento de puntos
     */
    public static class PointsAwardResponse {
        private Integer puntosOtorgados;
        private Integer balanceTotal;
        private String message;

        public PointsAwardResponse() {}

        public Integer getPuntosOtorgados() {
            return puntosOtorgados;
        }

        public void setPuntosOtorgados(Integer puntosOtorgados) {
            this.puntosOtorgados = puntosOtorgados;
        }

        public Integer getBalanceTotal() {
            return balanceTotal;
        }

        public void setBalanceTotal(Integer balanceTotal) {
            this.balanceTotal = balanceTotal;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}