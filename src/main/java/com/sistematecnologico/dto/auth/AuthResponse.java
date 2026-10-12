package com.sistematecnologico.dto.auth;

import com.sistematecnologico.entity.Rol;

public record AuthResponse(
        String token,
        String tipo,
        Long id,
        String nombre,
        String email,
        Rol rol
) {
    public AuthResponse(String token, Long id, String nombre, String email, Rol rol) {
        this(token, "Bearer", id, nombre, email, rol);
    }
}
