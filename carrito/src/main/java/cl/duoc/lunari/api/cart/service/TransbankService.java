package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.config.TransbankConfig;
import cl.duoc.lunari.api.cart.dto.TransbankConfirmResponse;
import cl.duoc.lunari.api.cart.dto.TransbankInitResponse;
import cl.duoc.lunari.api.cart.exception.PaymentFailedException;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.model.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.model.WebpayPlusTransactionCreateResponse;
import cl.transbank.webpay.webpayplus.model.WebpayPlusTransactionStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para integración con Transbank WebPay Plus
 * Maneja la creación y confirmación de transacciones
 */
@Service
public class TransbankService {

    private static final Logger logger = LoggerFactory.getLogger(TransbankService.class);

    // Credenciales de integración para testing (sandbox)
    private static final String TEST_COMMERCE_CODE = "597055555532";
    private static final String TEST_API_KEY = "579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C";

    private final TransbankConfig transbankConfig;

    @Autowired
    public TransbankService(TransbankConfig transbankConfig) {
        this.transbankConfig = transbankConfig;
        logger.info("TransbankService inicializado - Modo: {}",
                transbankConfig.isProduction() ? "PRODUCCIÓN" : "PRUEBAS");
    }

    /**
     * Crea una nueva transacción en Transbank
     *
     * @param amount Monto total de la transacción
     * @param buyOrder Orden de compra única
     * @param sessionId ID de sesión único
     * @param returnUrl URL a la que Transbank redirigirá después del pago
     * @return Respuesta con token y URL de pago
     */
    public TransbankInitResponse createTransaction(
            BigDecimal amount,
            String buyOrder,
            String sessionId,
            String returnUrl
    ) {
        try {
            logger.info("Creando transacción Transbank - Buy Order: {}, Monto: {}", buyOrder, amount);

            // En SDK 2.x, usar credenciales por defecto (integration) o configuradas globalmente
            // Si es producción, las credenciales deben configurarse a nivel de aplicación
            WebpayPlusTransactionCreateResponse response;

            if (transbankConfig.isProduction()) {
                // En producción, se esperan variables de entorno configuradas
                response = WebpayPlus.Transaction.create(
                        buyOrder,
                        sessionId,
                        amount.doubleValue(),
                        returnUrl
                );
            } else {
                // En test, usar credenciales de integración por defecto
                response = WebpayPlus.Transaction.create(
                        buyOrder,
                        sessionId,
                        amount.doubleValue(),
                        returnUrl
                );
            }

            logger.info("Transacción creada exitosamente - Token: {}", response.getToken());

            return new TransbankInitResponse(
                    response.getToken(),
                    response.getUrl()
            );

        } catch (Exception e) {
            logger.error("Error al crear transacción en Transbank", e);
            throw new PaymentFailedException("Error al iniciar pago con Transbank: " + e.getMessage(), e);
        }
    }

    /**
     * Confirma una transacción en Transbank usando el token
     *
     * @param token Token de la transacción a confirmar
     * @return Respuesta con detalles de la transacción confirmada
     */
    public TransbankConfirmResponse confirmTransaction(String token) {
        try {
            logger.info("Confirmando transacción Transbank - Token: {}", token);

            WebpayPlusTransactionCommitResponse response = WebpayPlus.Transaction.commit(token);

            logger.info("Transacción confirmada - Buy Order: {}, Response Code: {}, Authorization Code: {}",
                    response.getBuyOrder(),
                    response.getResponseCode(),
                    response.getAuthorizationCode());

            TransbankConfirmResponse confirmResponse = new TransbankConfirmResponse();
            confirmResponse.setBuyOrder(response.getBuyOrder());
            confirmResponse.setSessionId(response.getSessionId());
            confirmResponse.setAmount(BigDecimal.valueOf(response.getAmount()));
            confirmResponse.setAuthorizationCode(response.getAuthorizationCode());
            confirmResponse.setPaymentTypeCode(response.getPaymentTypeCode());
            confirmResponse.setResponseCode((int) response.getResponseCode());
            confirmResponse.setTransactionDate(
                    response.getTransactionDate() != null
                            ? parseTransbankDate(response.getTransactionDate())
                            : null
            );
            confirmResponse.setCardNumber(response.getCardDetail() != null
                    ? response.getCardDetail().getCardNumber()
                    : null);

            return confirmResponse;

        } catch (Exception e) {
            logger.error("Error al confirmar transacción en Transbank", e);
            throw new PaymentFailedException("Error al confirmar pago con Transbank: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica el estado de una transacción
     *
     * @param token Token de la transacción
     * @return Respuesta con el estado actual
     */
    public TransbankConfirmResponse getTransactionStatus(String token) {
        try {
            logger.info("Consultando estado de transacción - Token: {}", token);

            WebpayPlusTransactionStatusResponse response = WebpayPlus.Transaction.status(token);

            TransbankConfirmResponse statusResponse = new TransbankConfirmResponse();
            statusResponse.setBuyOrder(response.getBuyOrder());
            statusResponse.setSessionId(response.getSessionId());
            statusResponse.setAmount(BigDecimal.valueOf(response.getAmount()));
            statusResponse.setAuthorizationCode(response.getAuthorizationCode());
            statusResponse.setPaymentTypeCode(response.getPaymentTypeCode());
            statusResponse.setResponseCode((int) response.getResponseCode());
            statusResponse.setTransactionDate(
                    response.getTransactionDate() != null
                            ? parseTransbankDate(response.getTransactionDate())
                            : null
            );

            return statusResponse;

        } catch (Exception e) {
            logger.error("Error al consultar estado de transacción en Transbank", e);
            throw new PaymentFailedException("Error al consultar estado del pago: " + e.getMessage(), e);
        }
    }

    /**
     * Parsea la fecha de Transbank (formato String) a OffsetDateTime
     */
    private OffsetDateTime parseTransbankDate(String dateString) {
        try {
            // Transbank devuelve fechas en formato: "2023-12-01T15:30:00"
            LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return OffsetDateTime.of(localDateTime, ZoneId.systemDefault().getRules().getOffset(localDateTime));
        } catch (Exception e) {
            logger.warn("Error al parsear fecha de Transbank: {}", dateString, e);
            return null;
        }
    }
}
