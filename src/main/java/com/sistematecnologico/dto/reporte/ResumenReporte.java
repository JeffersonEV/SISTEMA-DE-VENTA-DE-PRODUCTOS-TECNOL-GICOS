package com.sistematecnologico.dto.reporte;

import java.math.BigDecimal;

public record ResumenReporte(
        BigDecimal totalVentas,
        long totalPedidos,
        long pedidosPendientes,
        long pedidosPagados,
        long pedidosCancelados,
        long totalUsuarios,
        long totalProductos
) {
}
