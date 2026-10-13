package com.sistematecnologico.service;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.inventario.MovimientoRequest;
import com.sistematecnologico.dto.inventario.MovimientoResponse;
import com.sistematecnologico.entity.MovimientoInventario;
import com.sistematecnologico.entity.Producto;
import com.sistematecnologico.entity.TipoMovimiento;
import com.sistematecnologico.exception.RecursoNoEncontradoException;
import com.sistematecnologico.exception.ReglaNegocioException;
import com.sistematecnologico.repository.MovimientoInventarioRepository;
import com.sistematecnologico.repository.ProductoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public InventarioService(MovimientoInventarioRepository movimientoRepository,
                             ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<MovimientoResponse> listar(Pageable pageable) {
        return PageResponse.from(movimientoRepository.findAllByOrderByFechaDescIdDesc(pageable), this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> porProducto(Long productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaDescIdDesc(productoId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + request.productoId()));

        int stockAnterior = producto.getStock();
        int cantidad = request.cantidad();
        int stockNuevo;

        switch (request.tipo()) {
            case ENTRADA -> {
                if (cantidad <= 0) {
                    throw new ReglaNegocioException("La cantidad de entrada debe ser mayor a cero");
                }
                stockNuevo = stockAnterior + cantidad;
            }
            case SALIDA -> {
                if (cantidad <= 0) {
                    throw new ReglaNegocioException("La cantidad de salida debe ser mayor a cero");
                }
                if (cantidad > stockAnterior) {
                    throw new ReglaNegocioException("Stock insuficiente. Disponible: " + stockAnterior);
                }
                stockNuevo = stockAnterior - cantidad;
            }
            case AJUSTE -> stockNuevo = cantidad;
            default -> throw new ReglaNegocioException("Tipo de movimiento no soportado");
        }

        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .tipo(request.tipo())
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(request.motivo())
                .build();
        return toResponse(movimientoRepository.save(movimiento));
    }

    MovimientoInventario registrarAutomatico(Producto producto, TipoMovimiento tipo, int cantidad,
                                             int stockAnterior, int stockNuevo, String motivo, Long pedidoId) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .tipo(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(motivo)
                .referenciaPedidoId(pedidoId)
                .build();
        return movimientoRepository.save(movimiento);
    }

    private MovimientoResponse toResponse(MovimientoInventario m) {
        return new MovimientoResponse(
                m.getId(),
                m.getProducto().getId(),
                m.getProducto().getNombre(),
                m.getTipo(),
                m.getCantidad(),
                m.getStockAnterior(),
                m.getStockNuevo(),
                m.getMotivo(),
                m.getReferenciaPedidoId(),
                m.getFecha()
        );
    }
}
