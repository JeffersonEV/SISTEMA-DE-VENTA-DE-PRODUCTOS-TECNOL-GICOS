package com.sistematecnologico.dto.producto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripcion no debe superar 500 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a 0")
        @DecimalMin(value = "0.01", message = "El precio minimo es 0.01")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 80)
        String marca,

        @Size(max = 500)
        String imagenUrl,

        Boolean activo,

        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId
) {
}
