package com.sistematecnologico.dto.reporte;

import java.math.BigDecimal;

public record TopProducto(
        Long productoId,
        String nombre,
        Long cantidadVendida,
        BigDecimal totalVendido
) {
}
