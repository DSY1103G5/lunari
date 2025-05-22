package cl.duoc.lunari.api.payload;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> fromOptional(
            Optional<T> optional,
            String errorMessage,
            HttpStatus errorStatus) {

        return optional
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .orElseGet(() -> ResponseEntity.status(errorStatus)
                        .body(ApiResponse.<T>error(errorMessage, errorStatus.value())));
    }

    public static <T> ResponseEntity<ApiResponse<T>> fromOptional(
            Optional<T> optional,
            String errorMessage) {
        return fromOptional(optional, errorMessage, HttpStatus.NOT_FOUND);
    }
}