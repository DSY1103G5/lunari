package cl.duoc.lunari.api.user.converter;

import cl.duoc.lunari.api.user.model.Coupon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class CouponListConverter implements AttributeConverter<List<Coupon>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // For LocalDateTime handling
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown fields like "valid"

    @Override
    public String convertToDatabaseColumn(List<Coupon> coupons) {
        if (coupons == null || coupons.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(coupons);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting coupons to JSON", e);
        }
    }

    @Override
    public List<Coupon> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || "[]".equals(dbData)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Coupon>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parsing coupons JSON: " + dbData, e);
        }
    }
}
