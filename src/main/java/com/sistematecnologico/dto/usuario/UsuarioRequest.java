package com.sistematecnologico.dto.usuario;

import com.sistematecnologico.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 80)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 80)
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 60)
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
