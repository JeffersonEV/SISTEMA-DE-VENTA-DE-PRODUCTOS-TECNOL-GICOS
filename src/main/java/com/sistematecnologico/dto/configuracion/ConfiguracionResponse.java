package com.sistematecnologico.dto.configuracion;

public record ConfiguracionResponse(
        Long id,
        String nombreTienda,
        String descripcion,
        String emailContacto,
        String telefono,
        String whatsapp,
        String direccion,
        String facebookUrl,
        String instagramUrl,
        String twitterUrl
) {
}
