package cl.duoc.lunari.api.user.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base converter for JSON/JSONB columns in PostgreSQL.
 *
 * This converter handles serialization and deserialization of Java objects
 * to/from JSON strings for storage in PostgreSQL JSONB columns.
 */
@Slf4j
public abstract class JpaJsonConverter<T> implements AttributeConverter<T, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> clazz;

    protected JpaJsonConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting {} to JSON", clazz.getSimpleName(), e);
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Error converting from JSON", e);
        }
    }
}
