package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.model.Pedido;
import cl.duoc.lunari.api.cart.service.client.UsuarioServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Job asíncrono para asignar puntos a usuarios
 * Se ejecuta después de un pago aprobado
 */
@Service
public class PointsAwardJob {

    private static final Logger logger = LoggerFactory.getLogger(PointsAwardJob.class);

    private final UsuarioServiceClient usuarioServiceClient;

    @Autowired
    public PointsAwardJob(UsuarioServiceClient usuarioServiceClient) {
        this.usuarioServiceClient = usuarioServiceClient;
    }

    /**
     * Ejecuta la asignación de puntos para un pedido de forma asíncrona
     * Utiliza best-effort: si falla, registra el error pero no bloquea el flujo
     *
     * @param pedido Pedido que generó los puntos a asignar
     */
    @Async("taskExecutor")
    public void execute(Pedido pedido) {
        logger.info("Iniciando asignación de puntos para pedido: {}", pedido.getNumeroPedido());

        try {
            // Obtener puntos calculados del pedido
            Integer puntosAOtorgar = pedido.getTotalPuntosGanados();

            if (puntosAOtorgar == null || puntosAOtorgar <= 0) {
                logger.info("No hay puntos para otorgar en pedido: {}", pedido.getNumeroPedido());
                return;
            }

            // Llamar al servicio de usuario para otorgar puntos
            boolean success = usuarioServiceClient.awardPoints(
                    pedido.getUsuarioId(),
                    puntosAOtorgar,
                    pedido.getNumeroPedido()
            );

            if (success) {
                logger.info("Puntos otorgados exitosamente para pedido: {} - Puntos: {}",
                        pedido.getNumeroPedido(), puntosAOtorgar);
            } else {
                logger.error("Falló la asignación de puntos para pedido: {}. " +
                        "Se requiere reconciliación manual.", pedido.getNumeroPedido());

                // TODO: Opcional - Marcar pedido para reconciliación manual
                // TODO: Opcional - Enviar notificación a administradores o usuario
            }

        } catch (Exception e) {
            logger.error("Error inesperado en asignación de puntos para pedido: {}",
                    pedido.getNumeroPedido(), e);

            // TODO: Opcional - Registrar en tabla de fallos para retry posterior
            // TODO: Opcional - Enviar alerta
        }
    }

    /**
     * Consulta el balance de puntos actual de un usuario
     * Este método es síncrono y puede usarse para validaciones
     *
     * @param usuarioId ID del usuario
     * @return Balance de puntos actual
     */
    public Integer getUserPointsBalance(java.util.UUID usuarioId) {
        try {
            return usuarioServiceClient.getPointsBalance(usuarioId);
        } catch (Exception e) {
            logger.error("Error al consultar balance de puntos del usuario: {}", usuarioId, e);
            return 0;
        }
    }
}
