package com.sistematecnologico.dto.usuario;

import com.sistematecnologico.entity.Rol;

import java.time.LocalDateTime;

/**
 * DTO de salida. Nunca incluye la contrasena.
 */
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        Boolean activo,
        Rol rol,
        LocalDateTime fechaRegistro
) {
}
