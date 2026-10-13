package com.sistematecnologico.dto.banner;

public record BannerResponse(
        Long id,
        String titulo,
        String subtitulo,
        String imagenUrl,
        String enlaceUrl,
        String textoBoton,
        Integer orden,
        Boolean activo
) {
}
