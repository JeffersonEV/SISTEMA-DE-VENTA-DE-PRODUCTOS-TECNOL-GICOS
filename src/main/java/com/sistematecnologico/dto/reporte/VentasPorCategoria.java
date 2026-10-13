package com.sistematecnologico.dto.reporte;

import java.math.BigDecimal;

public record VentasPorCategoria(
        String categoria,
        Long cantidadVendida,
        BigDecimal totalVendido
) {
}
