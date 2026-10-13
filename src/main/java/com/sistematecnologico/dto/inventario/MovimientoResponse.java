package com.sistematecnologico.dto.inventario;

import com.sistematecnologico.entity.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        Long productoId,
        String productoNombre,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockNuevo,
        String motivo,
        Long referenciaPedidoId,
        LocalDateTime fecha
) {
}
