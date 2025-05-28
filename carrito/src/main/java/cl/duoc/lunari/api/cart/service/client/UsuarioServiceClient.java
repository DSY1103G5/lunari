package cl.duoc.lunari.api.cart.service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
}