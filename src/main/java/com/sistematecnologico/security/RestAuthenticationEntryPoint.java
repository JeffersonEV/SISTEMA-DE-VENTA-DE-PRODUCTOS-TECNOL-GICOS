package com.sistematecnologico.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Devuelve 401 en JSON cuando falta un token valido.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = """
                {"timestamp":"%s","status":401,"error":"Unauthorized","mensaje":"No autenticado o token invalido","path":"%s"}
                """.formatted(LocalDateTime.now(), request.getRequestURI());
        response.getWriter().write(json);
    }
}
