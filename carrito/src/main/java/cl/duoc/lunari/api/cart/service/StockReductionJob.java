package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.dto.StockReductionRequest;
import cl.duoc.lunari.api.cart.model.Pedido;
import cl.duoc.lunari.api.cart.model.PedidoItem;
import cl.duoc.lunari.api.cart.service.client.InventarioServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Job asíncrono para reducir stock en el servicio de inventario
 * Se ejecuta después de un pago aprobado
 */
@Service
public class StockReductionJob {

    private static final Logger logger = LoggerFactory.getLogger(StockReductionJob.class);

    private final InventarioServiceClient inventarioServiceClient;

    @Autowired
    public StockReductionJob(InventarioServiceClient inventarioServiceClient) {
        this.inventarioServiceClient = inventarioServiceClient;
    }

    /**
     * Ejecuta la reducción de stock para un pedido de forma asíncrona
     * Utiliza best-effort: si falla, registra el error pero no bloquea el flujo
     *
     * @param pedido Pedido con los items cuyo stock debe reducirse
     */
    @Async("taskExecutor")
    public void execute(Pedido pedido) {
        logger.info("Iniciando reducción de stock para pedido: {}", pedido.getNumeroPedido());

        try {
            // Convertir items del pedido a formato de reducción de stock
            List<StockReductionRequest.StockItem> stockItems = new ArrayList<>();

            for (PedidoItem item : pedido.getItems()) {
                StockReductionRequest.StockItem stockItem = new StockReductionRequest.StockItem(
                        item.getProductoId(),
                        item.getCantidad()
                );
                stockItems.add(stockItem);
            }

            // Llamar al servicio de inventario
            boolean success = inventarioServiceClient.reduceStock(stockItems);

            if (success) {
                logger.info("Stock reducido exitosamente para pedido: {}", pedido.getNumeroPedido());
            } else {
                logger.error("Falló la reducción de stock para pedido: {}. " +
                        "Se requiere reconciliación manual.", pedido.getNumeroPedido());

                // TODO: Opcional - Marcar pedido para reconciliación manual
                // TODO: Opcional - Enviar notificación a administradores
            }

        } catch (Exception e) {
            logger.error("Error inesperado en reducción de stock para pedido: {}",
                    pedido.getNumeroPedido(), e);

            // TODO: Opcional - Registrar en tabla de fallos para retry posterior
            // TODO: Opcional - Enviar alerta
        }
    }

    /**
     * Verifica si hay stock suficiente para un pedido antes de crearlo
     * Este método es síncrono y se llama antes de procesar el pago
     *
     * @param pedido Pedido a verificar
     * @return true si hay stock suficiente para todos los items
     */
    public boolean verifyStockAvailability(Pedido pedido) {
        logger.info("Verificando disponibilidad de stock para pedido: {}", pedido.getNumeroPedido());

        try {
            for (PedidoItem item : pedido.getItems()) {
                boolean hasStock = inventarioServiceClient.checkStock(
                        item.getProductoId(),
                        item.getCantidad()
                );

                if (!hasStock) {
                    logger.warn("Stock insuficiente para producto {} en pedido {}",
                            item.getProductoId(), pedido.getNumeroPedido());
                    return false;
                }
            }

            logger.info("Stock verificado OK para pedido: {}", pedido.getNumeroPedido());
            return true;

        } catch (Exception e) {
            logger.error("Error al verificar stock para pedido: {}", pedido.getNumeroPedido(), e);
            // En caso de error de comunicación, asumimos que no hay stock para evitar sobreventa
            return false;
        }
    }
}
