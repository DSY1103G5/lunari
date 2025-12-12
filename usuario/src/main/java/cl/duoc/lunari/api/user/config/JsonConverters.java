package cl.duoc.lunari.api.user.config;

import cl.duoc.lunari.api.user.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * JSON/JSONB converters for PostgreSQL.
 *
 * These converters handle automatic serialization/deserialization of Java objects
 * to/from JSONB columns in PostgreSQL.
 */
public class JsonConverters {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converter for Personal JSONB column
     */
    @Converter(autoApply = true)
    @Slf4j
    public static class PersonalConverter implements AttributeConverter<Personal, String> {

        @Override
        public String convertToDatabaseColumn(Personal attribute) {
            if (attribute == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                log.error("Error converting Personal to JSON", e);
                throw new RuntimeException("Error converting Personal to JSON", e);
            }
        }

        @Override
        public Personal convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(dbData, Personal.class);
            } catch (JsonProcessingException e) {
                log.error("Error converting JSON to Personal", e);
                throw new RuntimeException("Error converting JSON to Personal", e);
            }
        }
    }

    /**
     * Converter for Address JSONB column
     */
    @Converter(autoApply = true)
    @Slf4j
    public static class AddressConverter implements AttributeConverter<Address, String> {

        @Override
        public String convertToDatabaseColumn(Address attribute) {
            if (attribute == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                log.error("Error converting Address to JSON", e);
                throw new RuntimeException("Error converting Address to JSON", e);
            }
        }

        @Override
        public Address convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(dbData, Address.class);
            } catch (JsonProcessingException e) {
                log.error("Error converting JSON to Address", e);
                throw new RuntimeException("Error converting JSON to Address", e);
            }
        }
    }

    /**
     * Converter for ClientPreferences JSONB column
     */
    @Converter(autoApply = true)
    @Slf4j
    public static class ClientPreferencesConverter implements AttributeConverter<ClientPreferences, String> {

        @Override
        public String convertToDatabaseColumn(ClientPreferences attribute) {
            if (attribute == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                log.error("Error converting ClientPreferences to JSON", e);
                throw new RuntimeException("Error converting ClientPreferences to JSON", e);
            }
        }

        @Override
        public ClientPreferences convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(dbData, ClientPreferences.class);
            } catch (JsonProcessingException e) {
                log.error("Error converting JSON to ClientPreferences", e);
                throw new RuntimeException("Error converting JSON to ClientPreferences", e);
            }
        }
    }

    /**
     * Converter for Gaming JSONB column
     */
    @Converter(autoApply = true)
    @Slf4j
    public static class GamingConverter implements AttributeConverter<Gaming, String> {

        @Override
        public String convertToDatabaseColumn(Gaming attribute) {
            if (attribute == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                log.error("Error converting Gaming to JSON", e);
                throw new RuntimeException("Error converting Gaming to JSON", e);
            }
        }

        @Override
        public Gaming convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(dbData, Gaming.class);
            } catch (JsonProcessingException e) {
                log.error("Error converting JSON to Gaming", e);
                throw new RuntimeException("Error converting JSON to Gaming", e);
            }
        }
    }

    /**
     * Converter for ClientStats JSONB column
     */
    @Converter(autoApply = true)
    @Slf4j
    public static class ClientStatsConverter implements AttributeConverter<ClientStats, String> {

        @Override
        public String convertToDatabaseColumn(ClientStats attribute) {
            if (attribute == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                log.error("Error converting ClientStats to JSON", e);
                throw new RuntimeException("Error converting ClientStats to JSON", e);
            }
        }

        @Override
        public ClientStats convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(dbData, ClientStats.class);
            } catch (JsonProcessingException e) {
                log.error("Error converting JSON to ClientStats", e);
                throw new RuntimeException("Error converting JSON to ClientStats", e);
            }
        }
    }

    /**
     * Converter for List<Coupon> JSONB column
     */
    @Converter(autoApply = true)
    @Slf4j
    public static class CouponListConverter implements AttributeConverter<List<Coupon>, String> {

        @Override
        public String convertToDatabaseColumn(List<Coupon> attribute) {
            if (attribute == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsString(attribute);
            } catch (JsonProcessingException e) {
                log.error("Error converting List<Coupon> to JSON", e);
                throw new RuntimeException("Error converting List<Coupon> to JSON", e);
            }
        }

        @Override
        public List<Coupon> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            try {
                return objectMapper.readValue(dbData, new TypeReference<List<Coupon>>() {});
            } catch (JsonProcessingException e) {
                log.error("Error converting JSON to List<Coupon>", e);
                throw new RuntimeException("Error converting JSON to List<Coupon>", e);
            }
        }
    }
}
