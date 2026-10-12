package com.sistematecnologico.controller;

import com.sistematecnologico.dto.pedido.PedidoRequest;
import com.sistematecnologico.dto.pedido.PedidoResponse;
import com.sistematecnologico.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Creacion, historial y cancelacion de pedidos del cliente")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Crea un pedido con lista de productos (descuenta stock)")
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request,
                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crear(authentication.getName(), request));
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Historial de pedidos del usuario autenticado")
    public ResponseEntity<List<PedidoResponse>> misPedidos(Authentication authentication) {
        return ResponseEntity.ok(pedidoService.misPedidos(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un pedido (propio o cualquier pedido si es ADMIN)")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(pedidoService.obtener(id, authentication.getName(), esAdmin(authentication)));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela un pedido PENDIENTE (devuelve el stock)")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(pedidoService.cancelar(id, authentication.getName(), esAdmin(authentication)));
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}