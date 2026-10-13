package com.sistematecnologico.dto.categoria;

import java.util.List;

public record CategoriaResponse(
        Long id,
        String nombre,
        String descripcion,
        Long padreId,
        String padreNombre,
        List<CategoriaResponse> subcategorias
) {
}
