package com.sistematecnologico.repository;

import com.sistematecnologico.dto.reporte.TopProducto;
import com.sistematecnologico.dto.reporte.VentasPorCategoria;
import com.sistematecnologico.entity.DetallePedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("""
            SELECT new com.sistematecnologico.dto.reporte.TopProducto(
                       dp.producto.id, dp.producto.nombre, SUM(dp.cantidad), SUM(dp.subtotal))
            FROM DetallePedido dp
            WHERE dp.pedido.estado <> com.sistematecnologico.entity.EstadoPedido.CANCELADO
            GROUP BY dp.producto.id, dp.producto.nombre
            ORDER BY SUM(dp.cantidad) DESC
            """)
    List<TopProducto> topProductos(Pageable pageable);

    @Query("""
            SELECT new com.sistematecnologico.dto.reporte.VentasPorCategoria(
                       c.nombre, SUM(dp.cantidad), SUM(dp.subtotal))
            FROM DetallePedido dp
            JOIN dp.producto p
            JOIN p.categoria c
            WHERE dp.pedido.estado <> com.sistematecnologico.entity.EstadoPedido.CANCELADO
            GROUP BY c.nombre
            ORDER BY SUM(dp.subtotal) DESC
            """)
    List<VentasPorCategoria> ventasPorCategoria();

    boolean existsByProductoId(Long productoId);
}
