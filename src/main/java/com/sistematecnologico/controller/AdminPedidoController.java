package com.sistematecnologico.controller;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.pedido.EstadoPedidoRequest;
import com.sistematecnologico.dto.pedido.PedidoResponse;
import com.sistematecnologico.entity.EstadoPedido;
import com.sistematecnologico.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/pedidos")
@Tag(name = "Admin - Pedidos", description = "Gestion de pedidos (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminPedidoController {

    private final PedidoService pedidoService;

    public AdminPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los pedidos con filtros (estado, usuario, fechas)")
    public ResponseEntity<PageResponse<PedidoResponse>> listar(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.adminListar(estado, usuarioId, desde, hasta, pageable));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado de un pedido (PENDIENTE/PAGADO/CANCELADO)")
    public ResponseEntity<PedidoResponse> cambiarEstado(@PathVariable Long id,
                                                        @Valid @RequestBody EstadoPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.adminCambiarEstado(id, request.estado()));
    }
}
