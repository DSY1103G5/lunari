package cl.duoc.lunari.api.cart.service;

import cl.duoc.lunari.api.cart.model.Carrito;
import cl.duoc.lunari.api.cart.model.CarritoItem;
import cl.duoc.lunari.api.cart.model.CarritoServicioAdicional;
import cl.duoc.lunari.api.cart.model.EstadoCarrito;
import cl.duoc.lunari.api.cart.repository.CarritoRepository;
import cl.duoc.lunari.api.cart.repository.CarritoItemRepository;
import cl.duoc.lunari.api.cart.repository.CarritoServicioAdicionalRepository;
import cl.duoc.lunari.api.cart.service.client.UsuarioServiceClient;
import cl.duoc.lunari.api.cart.service.client.InventarioServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final CarritoServicioAdicionalRepository carritoServicioAdicionalRepository;
    private final UsuarioServiceClient usuarioServiceClient;
    private final InventarioServiceClient inventarioServiceClient;

    /**
     * Obtiene o crea un carrito activo para un usuario
     */
    public Carrito obtenerOCrearCarritoActivo(UUID usuarioId) {
        log.info("Obteniendo o creando carrito activo para usuario: {}", usuarioId);
        
        // Verificar que el usuario existe
        if (!usuarioServiceClient.existeUsuario(usuarioId)) {
            throw new RuntimeException("Usuario no encontrado: " + usuarioId);
        }

        return carritoRepository.findCarritoActivoByUsuarioId(usuarioId)
                .orElseGet(() -> crearNuevoCarrito(usuarioId));
    }

    /**
     * Crea un nuevo carrito para un usuario
     */
    private Carrito crearNuevoCarrito(UUID usuarioId) {
        log.info("Creando nuevo carrito para usuario: {}", usuarioId);
        
        Carrito carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setTotalEstimado(BigDecimal.ZERO);
        carrito.setFechaExpiracion(OffsetDateTime.now().plusDays(30));
        
        return carritoRepository.save(carrito);
    }

    /**
     * Agrega un item al carrito
     */
    public CarritoItem agregarItemAlCarrito(UUID carritoId, Integer servicioId, 
                                          Integer cantidad, String personalizaciones) {
        log.info("Agregando item al carrito: carritoId={}, servicioId={}, cantidad={}", 
                carritoId, servicioId, cantidad);

        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));

        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new RuntimeException("El carrito no está activo");
        }

        // Verificar que el servicio existe en inventario
        var servicioInfo = inventarioServiceClient.obtenerServicio(servicioId);
        if (servicioInfo == null) {
            throw new RuntimeException("Servicio no encontrado: " + servicioId);
        }

        // Verificar si el item ya existe en el carrito
        Optional<CarritoItem> itemExistente = carritoItemRepository
                .findByCarritoIdAndServicioId(carritoId, servicioId);

        CarritoItem item;
        if (itemExistente.isPresent()) {
            // Actualizar cantidad del item existente
            item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPersonalizaciones(personalizaciones);
        } else {
            // Crear nuevo item
            item = new CarritoItem();
            item.setCarrito(carrito);
            item.setServicioId(servicioId);
            item.setCantidad(cantidad);
            item.setPrecioUnitario(servicioInfo.getPrecio());
            item.setPersonalizaciones(personalizaciones);
        }

        // Calcular subtotal
        BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
        item.setSubtotal(subtotal);

        item = carritoItemRepository.save(item);
        
        // Actualizar total del carrito
        actualizarTotalCarrito(carritoId);
        
        return item;
    }

    /**
     * Actualiza la cantidad de un item en el carrito
     */
    public CarritoItem actualizarCantidadItem(UUID itemId, Integer nuevaCantidad) {
        log.info("Actualizando cantidad del item: itemId={}, nuevaCantidad={}", itemId, nuevaCantidad);

        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + itemId));

        if (item.getCarrito().getEstado() != EstadoCarrito.ACTIVO) {
            throw new RuntimeException("El carrito no está activo");
        }

        if (nuevaCantidad <= 0) {
            // Eliminar item si la cantidad es 0 o menor
            carritoItemRepository.delete(item);
            actualizarTotalCarrito(item.getCarrito().getId());
            return null;
        }

        item.setCantidad(nuevaCantidad);
        BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(nuevaCantidad));
        item.setSubtotal(subtotal);

        item = carritoItemRepository.save(item);
        actualizarTotalCarrito(item.getCarrito().getId());
        
        return item;
    }

    /**
     * Agrega un servicio adicional a un item del carrito
     */
    public CarritoServicioAdicional agregarServicioAdicional(UUID itemId, Integer servicioAdicionalId) {
        log.info("Agregando servicio adicional: itemId={}, servicioAdicionalId={}", 
                itemId, servicioAdicionalId);

        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + itemId));

        if (item.getCarrito().getEstado() != EstadoCarrito.ACTIVO) {
            throw new RuntimeException("El carrito no está activo");
        }

        // Verificar que el servicio adicional existe
        var servicioAdicionalInfo = inventarioServiceClient.obtenerServicioAdicional(servicioAdicionalId);
        if (servicioAdicionalInfo == null) {
            throw new RuntimeException("Servicio adicional no encontrado: " + servicioAdicionalId);
        }

        // Verificar si ya existe este servicio adicional para el item
        if (carritoServicioAdicionalRepository.existsByCarritoItemIdAndServicioAdicionalId(
                itemId, servicioAdicionalId)) {
            throw new RuntimeException("El servicio adicional ya está agregado al item");
        }

        CarritoServicioAdicional servicioAdicional = new CarritoServicioAdicional();
        servicioAdicional.setCarritoItem(item);
        servicioAdicional.setServicioAdicionalId(servicioAdicionalId);
        servicioAdicional.setPrecioAdicional(servicioAdicionalInfo.getPrecio());

        servicioAdicional = carritoServicioAdicionalRepository.save(servicioAdicional);
        
        // Actualizar total del carrito
        actualizarTotalCarrito(item.getCarrito().getId());
        
        return servicioAdicional;
    }

    /**
     * Elimina un item del carrito
     */
    public void eliminarItemDelCarrito(UUID itemId) {
        log.info("Eliminando item del carrito: itemId={}", itemId);

        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado: " + itemId));

        UUID carritoId = item.getCarrito().getId();
        carritoItemRepository.delete(item);
        actualizarTotalCarrito(carritoId);
    }

    /**
     * Elimina un servicio adicional de un item
     */
    public void eliminarServicioAdicional(UUID servicioAdicionalId) {
        log.info("Eliminando servicio adicional: id={}", servicioAdicionalId);

        CarritoServicioAdicional servicioAdicional = carritoServicioAdicionalRepository
                .findById(servicioAdicionalId)
                .orElseThrow(() -> new RuntimeException("Servicio adicional no encontrado: " + servicioAdicionalId));

        UUID carritoId = servicioAdicional.getCarritoItem().getCarrito().getId();
        carritoServicioAdicionalRepository.delete(servicioAdicional);
        actualizarTotalCarrito(carritoId);
    }

    /**
     * Actualiza el total estimado del carrito
     */
    private void actualizarTotalCarrito(UUID carritoId) {
        log.debug("Actualizando total del carrito: {}", carritoId);

        // Calcular subtotal de items
        BigDecimal subtotalItems = carritoItemRepository.calculateSubtotalByCarritoId(carritoId);
        if (subtotalItems == null) subtotalItems = BigDecimal.ZERO;

        // Calcular total de servicios adicionales
        BigDecimal totalServiciosAdicionales = carritoServicioAdicionalRepository
                .calculateTotalServiciosAdicionalesByCarritoId(carritoId);
        if (totalServiciosAdicionales == null) totalServiciosAdicionales = BigDecimal.ZERO;

        BigDecimal totalEstimado = subtotalItems.add(totalServiciosAdicionales);

        carritoRepository.findById(carritoId).ifPresent(carrito -> {
            carrito.setTotalEstimado(totalEstimado);
            carritoRepository.save(carrito);
        });
    }

    /**
     * Obtiene un carrito por ID
     */
    @Transactional(readOnly = true)
    public Optional<Carrito> obtenerCarritoPorId(UUID carritoId) {
        return carritoRepository.findById(carritoId);
    }

    /**
     * Obtiene todos los carritos de un usuario
     */
    @Transactional(readOnly = true)
    public List<Carrito> obtenerCarritosPorUsuario(UUID usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Procesa un carrito (cambia estado a PROCESADO)
     */
    public Carrito procesarCarrito(UUID carritoId) {
        log.info("Procesando carrito: {}", carritoId);

        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));

        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new RuntimeException("Solo se pueden procesar carritos activos");
        }

        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new RuntimeException("No se puede procesar un carrito vacío");
        }

        carrito.setEstado(EstadoCarrito.PROCESADO);
        return carritoRepository.save(carrito);
    }

    /**
     * Abandona un carrito (cambia estado a ABANDONADO)
     */
    public Carrito abandonarCarrito(UUID carritoId) {
        log.info("Abandonando carrito: {}", carritoId);

        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));

        carrito.setEstado(EstadoCarrito.ABANDONADO);
        return carritoRepository.save(carrito);
    }

    /**
     * Vacía un carrito (elimina todos los items)
     */
    public void vaciarCarrito(UUID carritoId) {
        log.info("Vaciando carrito: {}", carritoId);

        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado: " + carritoId));

        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new RuntimeException("Solo se pueden vaciar carritos activos");
        }

        carritoItemRepository.deleteByCarritoId(carritoId);
        carrito.setTotalEstimado(BigDecimal.ZERO);
        carritoRepository.save(carrito);
    }

    /**
     * Expira carritos que han pasado su fecha de expiración
     */
    @Transactional
    public int expirarCarritos() {
        log.info("Expirando carritos vencidos");

        List<Carrito> carritosExpirados = carritoRepository.findByFechaExpiracionBefore(OffsetDateTime.now());
        
        for (Carrito carrito : carritosExpirados) {
            if (carrito.getEstado() == EstadoCarrito.ACTIVO) {
                carrito.setEstado(EstadoCarrito.EXPIRADO);
                carritoRepository.save(carrito);
            }
        }

        return carritosExpirados.size();
    }

    /**
     * Obtiene estadísticas de carritos
     */
    @Transactional(readOnly = true)
    public CarritoEstadisticas obtenerEstadisticas() {
        long totalActivos = carritoRepository.countByEstado(EstadoCarrito.ACTIVO);
        long totalProcesados = carritoRepository.countByEstado(EstadoCarrito.PROCESADO);
        long totalAbandonados = carritoRepository.countByEstado(EstadoCarrito.ABANDONADO);
        long totalExpirados = carritoRepository.countByEstado(EstadoCarrito.EXPIRADO);

        return new CarritoEstadisticas(totalActivos, totalProcesados, totalAbandonados, totalExpirados);
    }

    /**
     * Clase interna para estadísticas
     */
    public record CarritoEstadisticas(
            long carritoActivos,
            long carritosProcesados,
            long carritosAbandonados,
            long carritosExpirados
    ) {}
}