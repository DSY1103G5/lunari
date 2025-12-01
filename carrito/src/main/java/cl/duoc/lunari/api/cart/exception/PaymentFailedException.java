package cl.duoc.lunari.api.cart.exception;

/**
 * Excepción lanzada cuando falla un pago
 */
public class PaymentFailedException extends RuntimeException {

    private final Integer responseCode;

    public PaymentFailedException(String message) {
        super(message);
        this.responseCode = null;
    }

    public PaymentFailedException(String message, Integer responseCode) {
        super(message + " (código de respuesta: " + responseCode + ")");
        this.responseCode = responseCode;
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
        this.responseCode = null;
    }

    public Integer getResponseCode() {
        return responseCode;
    }
}
