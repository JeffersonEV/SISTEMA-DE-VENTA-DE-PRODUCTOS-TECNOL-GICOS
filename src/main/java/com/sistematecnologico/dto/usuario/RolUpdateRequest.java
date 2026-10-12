package com.sistematecnologico.dto.usuario;

import com.sistematecnologico.entity.Rol;
import jakarta.validation.constraints.NotNull;

public record RolUpdateRequest(
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
