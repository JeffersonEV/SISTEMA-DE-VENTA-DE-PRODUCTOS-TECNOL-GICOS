package com.sistematecnologico.dto.inventario;

import com.sistematecnologico.entity.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoRequest(
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipo,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        Integer cantidad,

        @Size(max = 200, message = "El motivo no debe superar 200 caracteres")
        String motivo
) {
}
