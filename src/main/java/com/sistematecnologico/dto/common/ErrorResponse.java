package com.sistematecnologico.dto.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Respuesta de error uniforme. No expone detalles tecnicos (stack traces, SQL, etc.).
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        Map<String, String> errores
) {
    public ErrorResponse(int status, String error, String mensaje, String path) {
        this(LocalDateTime.now(), status, error, mensaje, path, null);
    }

    public ErrorResponse(int status, String error, String mensaje, String path, Map<String, String> errores) {
        this(LocalDateTime.now(), status, error, mensaje, path, errores);
    }
}
