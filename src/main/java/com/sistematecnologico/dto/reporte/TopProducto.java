package com.sistematecnologico.dto.reporte;

import java.math.BigDecimal;

/**
 * Proyeccion JPQL (constructor expression) para productos mas vendidos.
 */
public record TopProducto(
        Long productoId,
        String nombre,
        Long cantidadVendida,
        BigDecimal totalVendido
) {
}
