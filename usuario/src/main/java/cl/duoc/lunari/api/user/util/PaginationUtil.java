package cl.duoc.lunari.api.user.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para codificar y decodificar tokens de paginación de DynamoDB.
 *
 * DynamoDB utiliza lastEvaluatedKey para paginación, que es un Map<String, AttributeValue>.
 * Este util convierte ese map a un token de string codificado en Base64 para
 * enviarlo al cliente, y viceversa.
 */
public class PaginationUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Codifica un lastEvaluatedKey de DynamoDB a un token de paginación.
     *
     * @param lastEvaluatedKey Map de atributos retornado por DynamoDB
     * @return Token codificado en Base64, o null si lastEvaluatedKey es null/vacío
     */
    public static String encodePaginationToken(Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }

        try {
            // Convertir el map a un formato serializable
            Map<String, String> serializableMap = new HashMap<>();
            lastEvaluatedKey.forEach((key, value) -> {
                if (value.s() != null) {
                    serializableMap.put(key, "S:" + value.s());
                } else if (value.n() != null) {
                    serializableMap.put(key, "N:" + value.n());
                } else if (value.bool() != null) {
                    serializableMap.put(key, "BOOL:" + value.bool());
                }
            });

            // Serializar a JSON y codificar en Base64
            String json = OBJECT_MAPPER.writeValueAsString(serializableMap);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al codificar token de paginación", e);
        }
    }

    /**
     * Decodifica un token de paginación a lastEvaluatedKey de DynamoDB.
     *
     * @param paginationToken Token codificado en Base64
     * @return Map de atributos para usar como exclusiveStartKey, o null si el token es null/vacío
     * @throws RuntimeException si el token es inválido
     */
    public static Map<String, AttributeValue> decodePaginationToken(String paginationToken) {
        if (paginationToken == null || paginationToken.trim().isEmpty()) {
            return null;
        }

        try {
            // Decodificar Base64 y deserializar JSON
            byte[] decodedBytes = Base64.getUrlDecoder().decode(paginationToken);
            String json = new String(decodedBytes);

            @SuppressWarnings("unchecked")
            Map<String, String> serializableMap = OBJECT_MAPPER.readValue(json, Map.class);

            // Convertir de vuelta a AttributeValue
            Map<String, AttributeValue> attributeMap = new HashMap<>();
            serializableMap.forEach((key, value) -> {
                if (value.startsWith("S:")) {
                    attributeMap.put(key, AttributeValue.builder().s(value.substring(2)).build());
                } else if (value.startsWith("N:")) {
                    attributeMap.put(key, AttributeValue.builder().n(value.substring(2)).build());
                } else if (value.startsWith("BOOL:")) {
                    attributeMap.put(key, AttributeValue.builder().bool(Boolean.parseBoolean(value.substring(5))).build());
                }
            });

            return attributeMap;

        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Token de paginación inválido", e);
        }
    }
}
