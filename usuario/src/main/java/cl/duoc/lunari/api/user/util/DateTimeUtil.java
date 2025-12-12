package cl.duoc.lunari.api.user.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utilidad para conversión entre OffsetDateTime y String ISO 8601.
 *
 * Facilita la conversión bidireccional para trabajar con
 * objetos Java OffsetDateTime en la capa de aplicación.
 */
public class DateTimeUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * Convierte OffsetDateTime a String ISO 8601.
     *
     * @param dateTime Objeto OffsetDateTime a convertir
     * @return String en formato ISO 8601, o null si dateTime es null
     */
    public static String toIsoString(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_FORMATTER) : null;
    }

    /**
     * Convierte String ISO 8601 a OffsetDateTime.
     *
     * @param isoString String en formato ISO 8601
     * @return OffsetDateTime parseado, o null si isoString es null o vacío
     * @throws DateTimeParseException si el formato es inválido
     */
    public static OffsetDateTime fromIsoString(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return null;
        }
        return OffsetDateTime.parse(isoString, ISO_FORMATTER);
    }

    /**
     * Obtiene el timestamp actual en formato ISO 8601.
     *
     * @return String con el timestamp actual
     */
    public static String now() {
        return OffsetDateTime.now().format(ISO_FORMATTER);
    }

    /**
     * Valida si un string es un timestamp ISO 8601 válido.
     *
     * @param isoString String a validar
     * @return true si es un formato válido, false en caso contrario
     */
    public static boolean isValidIsoString(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return false;
        }
        try {
            OffsetDateTime.parse(isoString, ISO_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
