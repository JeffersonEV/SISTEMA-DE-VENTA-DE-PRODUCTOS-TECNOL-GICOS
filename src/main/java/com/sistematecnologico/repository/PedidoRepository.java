package com.sistematecnologico.repository;

import com.sistematecnologico.entity.EstadoPedido;
import com.sistematecnologico.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // ---------- Derived queries ----------
    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    long countByEstado(EstadoPedido estado);

    // ---------- JPQL ----------

    /** Historial de pedidos de un usuario ordenado por fecha descendente. */
    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId ORDER BY p.fecha DESC")
    List<Pedido> historialPorUsuario(@Param("usuarioId") Long usuarioId);

    /** Pedidos filtrados por estado y rango de fechas. */
    @Query("SELECT p FROM Pedido p WHERE p.estado = :estado "
            + "AND p.fecha BETWEEN :desde AND :hasta ORDER BY p.fecha DESC")
    List<Pedido> porEstadoYRangoFechas(@Param("estado") EstadoPedido estado,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta);

    /** Filtro paginado para el panel administrativo. */
    @Query("""
            SELECT p FROM Pedido p
            WHERE (:estado IS NULL OR p.estado = :estado)
              AND (:usuarioId IS NULL OR p.usuario.id = :usuarioId)
              AND (:desde IS NULL OR p.fecha >= :desde)
              AND (:hasta IS NULL OR p.fecha <= :hasta)
            """)
    Page<Pedido> buscarAdmin(@Param("estado") EstadoPedido estado,
                             @Param("usuarioId") Long usuarioId,
                             @Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta,
                             Pageable pageable);

    /** Total de ventas (pedidos no cancelados). */
    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estado <> com.sistematecnologico.entity.EstadoPedido.CANCELADO")
    BigDecimal totalVentas();
}
