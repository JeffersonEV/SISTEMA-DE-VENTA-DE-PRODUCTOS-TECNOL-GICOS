package com.sistematecnologico.dto.reporte;

import java.math.BigDecimal;

/**
 * Proyeccion JPQL (constructor expression) para ventas agrupadas por categoria.
 */
public record VentasPorCategoria(
        String categoria,
        Long cantidadVendida,
        BigDecimal totalVendido
) {
}
