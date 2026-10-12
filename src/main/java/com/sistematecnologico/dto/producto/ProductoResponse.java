package com.sistematecnologico.dto.producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String marca,
        String imagenUrl,
        Boolean activo,
        Long categoriaId,
        String categoriaNombre
) {
}
