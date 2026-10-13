package com.sistematecnologico.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripcion no debe superar 255 caracteres")
        String descripcion,

        Long padreId
) {
}
