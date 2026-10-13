package com.sistematecnologico.dto.usuario;

import com.sistematecnologico.entity.Rol;

import java.time.LocalDateTime;

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
