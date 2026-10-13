package com.sistematecnologico.dto.configuracion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ConfiguracionRequest(
        @Size(max = 120, message = "El nombre de la tienda no debe superar 120 caracteres")
        String nombreTienda,

        @Size(max = 500, message = "La descripcion no debe superar 500 caracteres")
        String descripcion,

        @Email(message = "El email de contacto no es valido")
        @Size(max = 120, message = "El email no debe superar 120 caracteres")
        String emailContacto,

        @Size(max = 40, message = "El telefono no debe superar 40 caracteres")
        String telefono,

        @Size(max = 40, message = "El whatsapp no debe superar 40 caracteres")
        String whatsapp,

        @Size(max = 200, message = "La direccion no debe superar 200 caracteres")
        String direccion,

        @Size(max = 500, message = "La URL de Facebook no debe superar 500 caracteres")
        String facebookUrl,

        @Size(max = 500, message = "La URL de Instagram no debe superar 500 caracteres")
        String instagramUrl,

        @Size(max = 500, message = "La URL de Twitter no debe superar 500 caracteres")
        String twitterUrl
) {
}
