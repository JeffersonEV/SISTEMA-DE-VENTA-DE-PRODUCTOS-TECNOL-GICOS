package com.sistematecnologico.controller;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.inventario.MovimientoRequest;
import com.sistematecnologico.dto.inventario.MovimientoResponse;
import com.sistematecnologico.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventario")
@Tag(name = "Admin Inventario", description = "Movimientos y control de inventario (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminInventarioController {

    private final InventarioService inventarioService;

    public AdminInventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/movimientos")
    @Operation(summary = "Lista los movimientos de inventario con paginacion (ADMIN)")
    public ResponseEntity<PageResponse<MovimientoResponse>> listar(@PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(inventarioService.listar(pageable));
    }

    @GetMapping("/productos/{productoId}/movimientos")
    @Operation(summary = "Lista los movimientos de un producto (ADMIN)")
    public ResponseEntity<List<MovimientoResponse>> porProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.porProducto(productoId));
    }

    @PostMapping("/movimientos")
    @Operation(summary = "Registra un movimiento manual (entrada/salida/ajuste) (ADMIN)")
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.registrar(request));
    }
}
