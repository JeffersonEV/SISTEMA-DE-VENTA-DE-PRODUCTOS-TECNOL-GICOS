package com.sistematecnologico.controller;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.usuario.RolUpdateRequest;
import com.sistematecnologico.dto.usuario.UsuarioRequest;
import com.sistematecnologico.dto.usuario.UsuarioResponse;
import com.sistematecnologico.dto.usuario.UsuarioUpdateRequest;
import com.sistematecnologico.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Admin - Usuarios", description = "Gestion de usuarios (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios (paginado)")
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarPaginado(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crea un usuario asignando cualquier rol")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza datos basicos de un usuario")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/activo")
    @Operation(summary = "Activa/desactiva un usuario")
    public ResponseEntity<UsuarioResponse> cambiarActivo(@PathVariable Long id,
                                                         @RequestBody Map<String, Boolean> body) {
        boolean activo = body.getOrDefault("activo", true);
        return ResponseEntity.ok(usuarioService.cambiarActivo(id, activo));
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambia el rol de un usuario")
    public ResponseEntity<UsuarioResponse> cambiarRol(@PathVariable Long id,
                                                      @Valid @RequestBody RolUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
