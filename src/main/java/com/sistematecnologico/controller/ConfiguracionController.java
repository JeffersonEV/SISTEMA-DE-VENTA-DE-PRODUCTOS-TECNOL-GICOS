package com.sistematecnologico.controller;

import com.sistematecnologico.dto.configuracion.ConfiguracionRequest;
import com.sistematecnologico.dto.configuracion.ConfiguracionResponse;
import com.sistematecnologico.service.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "Configuracion", description = "Datos de la tienda (lectura publica, escritura ADMIN)")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    @Operation(summary = "Obtiene la configuracion de la tienda (publico)")
    public ResponseEntity<ConfiguracionResponse> obtener() {
        return ResponseEntity.ok(configuracionService.obtener());
    }

    @PutMapping
    @Operation(summary = "Actualiza la configuracion de la tienda (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ConfiguracionResponse> actualizar(@Valid @RequestBody ConfiguracionRequest request) {
        return ResponseEntity.ok(configuracionService.actualizar(request));
    }
}
