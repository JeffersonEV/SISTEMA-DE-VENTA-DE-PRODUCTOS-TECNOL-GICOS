package com.sistematecnologico.service;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.pedido.DetallePedidoResponse;
import com.sistematecnologico.dto.pedido.PedidoItemRequest;
import com.sistematecnologico.dto.pedido.PedidoRequest;
import com.sistematecnologico.dto.pedido.PedidoResponse;
import com.sistematecnologico.entity.*;
import com.sistematecnologico.exception.RecursoNoEncontradoException;
import com.sistematecnologico.exception.ReglaNegocioException;
import com.sistematecnologico.repository.PedidoRepository;
import com.sistematecnologico.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final InventarioService inventarioService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         UsuarioService usuarioService,
                         InventarioService inventarioService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.inventarioService = inventarioService;
    }

    @Transactional
    public PedidoResponse crear(String email, PedidoRequest request) {
        Usuario usuario = usuarioService.porEmail(email);

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (PedidoItemRequest item : request.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<MovimientoPendiente> pendientes = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cantidades.entrySet()) {
            Producto producto = productoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + entry.getKey()));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new ReglaNegocioException("El producto '" + producto.getNombre() + "' no esta disponible");
            }
            int cantidad = entry.getValue();
            if (producto.getStock() < cantidad) {
                throw new ReglaNegocioException("Stock insuficiente para '" + producto.getNombre()
                        + "'. Disponible: " + producto.getStock());
            }

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior - cantidad;
            producto.setStock(stockNuevo);
            productoRepository.save(producto);
            pendientes.add(new MovimientoPendiente(producto, cantidad, stockAnterior, stockNuevo));

            BigDecimal precio = producto.getPrecio();
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));

            DetallePedido detalle = DetallePedido.builder()
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precio)
                    .subtotal(subtotal)
                    .build();
            pedido.addDetalle(detalle);

            total = total.add(subtotal);
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);
        for (MovimientoPendiente p : pendientes) {
            inventarioService.registrarAutomatico(p.producto(), TipoMovimiento.SALIDA, p.cantidad(),
                    p.stockAnterior(), p.stockNuevo(), "Venta - Pedido #" + guardado.getId(), guardado.getId());
        }
        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> misPedidos(String email) {
        Usuario usuario = usuarioService.porEmail(email);
        return pedidoRepository.historialPorUsuario(usuario.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id, String email, boolean esAdmin) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));
        if (!esAdmin && !pedido.getUsuario().getEmail().equals(email)) {
            throw new AccessDeniedException("No puede acceder a un pedido de otro usuario");
        }
        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse cancelar(Long id, String email, boolean esAdmin) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));

        if (!esAdmin && !pedido.getUsuario().getEmail().equals(email)) {
            throw new AccessDeniedException("No puede cancelar un pedido de otro usuario");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }

        restaurarStock(pedido);
        pedido.setEstado(EstadoPedido.CANCELADO);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoResponse> adminListar(EstadoPedido estado, Long usuarioId,
                                                    LocalDateTime desde, LocalDateTime hasta,
                                                    Pageable pageable) {
        Page<Pedido> page = pedidoRepository.buscarAdmin(estado, usuarioId, desde, hasta, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Transactional
    public PedidoResponse adminCambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id " + id));

        if (nuevoEstado == EstadoPedido.CANCELADO && pedido.getEstado() != EstadoPedido.CANCELADO) {
            restaurarStock(pedido);
        }
        pedido.setEstado(nuevoEstado);
        return toResponse(pedidoRepository.save(pedido));
    }

    private void restaurarStock(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior + detalle.getCantidad();
            producto.setStock(stockNuevo);
            productoRepository.save(producto);
            inventarioService.registrarAutomatico(producto, TipoMovimiento.ENTRADA, detalle.getCantidad(),
                    stockAnterior, stockNuevo, "Cancelacion - Pedido #" + pedido.getId(), pedido.getId());
        }
    }

    public PedidoResponse toResponse(Pedido pedido) {
        List<DetallePedidoResponse> detalles = pedido.getDetalles().stream()
                .map(d -> new DetallePedidoResponse(
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()))
                .toList();

        String cliente = pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getUsuario().getId(),
                cliente,
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getTotal(),
                detalles);
    }

    private record MovimientoPendiente(Producto producto, int cantidad, int stockAnterior, int stockNuevo) {
    }
}
