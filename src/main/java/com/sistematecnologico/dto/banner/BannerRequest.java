package com.sistematecnologico.dto.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BannerRequest(
        @NotBlank(message = "El titulo del banner es obligatorio")
        @Size(max = 120, message = "El titulo no debe superar 120 caracteres")
        String titulo,

        @Size(max = 200, message = "El subtitulo no debe superar 200 caracteres")
        String subtitulo,

        @Size(max = 500, message = "La imagen no debe superar 500 caracteres")
        String imagenUrl,

        @Size(max = 500, message = "El enlace no debe superar 500 caracteres")
        String enlaceUrl,

        @Size(max = 80, message = "El texto del boton no debe superar 80 caracteres")
        String textoBoton,

        Integer orden,

        Boolean activo
) {
}
