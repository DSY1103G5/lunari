package cl.duoc.lunari.api.payload;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<K> {
    private boolean success;
    private K response;
    private String message;
    private int statusCode;


    public static <S> ApiResponse<S> success(S data) {
        return new ApiResponse<>(true, data, "Success", HttpStatus.OK.value());
    }

    public static <E> ApiResponse<E> error(String errorMessage, int statusCode) {
        return new ApiResponse<>(false, null, errorMessage, statusCode); // Data is null for error
    }
}