package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.model.Carrito;
import cl.duoc.lunari.api.cart.model.EstadoPedido;
import cl.duoc.lunari.api.cart.model.Pedido;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestión de pedidos
 */
public interface OrderService {

    /**
     * Crea un pedido a partir de un carrito
     *
     * @param carrito Carrito a convertir en pedido
     * @param notasCliente Notas opcionales del cliente
     * @return Pedido creado
     */
    Pedido createOrderFromCart(Carrito carrito, String notasCliente);

    /**
     * Obtiene un pedido por su ID
     *
     * @param orderId ID del pedido
     * @return Pedido encontrado
     */
    Pedido getOrderById(UUID orderId);

    /**
     * Obtiene un pedido por su número
     *
     * @param numeroPedido Número del pedido
     * @return Pedido encontrado
     */
    Pedido getOrderByNumero(String numeroPedido);

    /**
     * Obtiene todos los pedidos de un usuario
     *
     * @param usuarioId ID del usuario
     * @return Lista de pedidos
     */
    List<Pedido> getOrdersByUsuario(UUID usuarioId);

    /**
     * Obtiene pedidos de un usuario por estado
     *
     * @param usuarioId ID del usuario
     * @param estado Estado del pedido
     * @return Lista de pedidos
     */
    List<Pedido> getOrdersByUsuarioAndEstado(UUID usuarioId, EstadoPedido estado);

    /**
     * Actualiza el estado de un pedido
     *
     * @param orderId ID del pedido
     * @param nuevoEstado Nuevo estado
     * @return Pedido actualizado
     */
    Pedido updateOrderStatus(UUID orderId, EstadoPedido nuevoEstado);

    /**
     * Marca un pedido como completado
     *
     * @param orderId ID del pedido
     * @param puntosGanados Puntos ganados por el usuario
     * @return Pedido actualizado
     */
    Pedido markOrderComplete(UUID orderId, Integer puntosGanados);

    /**
     * Cancela un pedido
     *
     * @param orderId ID del pedido
     * @return Pedido cancelado
     */
    Pedido cancelOrder(UUID orderId);

    /**
     * Genera un número de pedido único
     *
     * @return Número de pedido en formato ORD-YYYYMMDD-XXXXX
     */
    String generateOrderNumber();
}
