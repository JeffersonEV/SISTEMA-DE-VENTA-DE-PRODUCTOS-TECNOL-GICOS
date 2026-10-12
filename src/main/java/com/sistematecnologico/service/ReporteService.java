package com.sistematecnologico.service;

import com.sistematecnologico.dto.reporte.ProductoStockBajo;
import com.sistematecnologico.dto.reporte.ResumenReporte;
import com.sistematecnologico.dto.reporte.TopProducto;
import com.sistematecnologico.dto.reporte.VentasPorCategoria;
import com.sistematecnologico.entity.EstadoPedido;
import com.sistematecnologico.repository.DetallePedidoRepository;
import com.sistematecnologico.repository.PedidoRepository;
import com.sistematecnologico.repository.ProductoRepository;
import com.sistematecnologico.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    public ReporteService(PedidoRepository pedidoRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository,
                          DetallePedidoRepository detallePedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Transactional(readOnly = true)
    public ResumenReporte resumen() {
        BigDecimal totalVentas = pedidoRepository.totalVentas();
        if (totalVentas == null) {
            totalVentas = BigDecimal.ZERO;
        }
        return new ResumenReporte(
                totalVentas,
                pedidoRepository.count(),
                pedidoRepository.countByEstado(EstadoPedido.PENDIENTE),
                pedidoRepository.countByEstado(EstadoPedido.PAGADO),
                pedidoRepository.countByEstado(EstadoPedido.CANCELADO),
                usuarioRepository.count(),
                productoRepository.count());
    }

    @Transactional(readOnly = true)
    public List<TopProducto> topProductos(int limite) {
        int size = limite <= 0 ? 5 : Math.min(limite, 50);
        return detallePedidoRepository.topProductos(PageRequest.of(0, size));
    }

    @Transactional(readOnly = true)
    public List<VentasPorCategoria> ventasPorCategoria() {
        return detallePedidoRepository.ventasPorCategoria();
    }

    @Transactional(readOnly = true)
    public List<ProductoStockBajo> stockBajo(int umbral) {
        return productoRepository.productosStockBajo(umbral).stream()
                .map(p -> new ProductoStockBajo(p.getId(), p.getNombre(), p.getStock()))
                .toList();
    }
}
