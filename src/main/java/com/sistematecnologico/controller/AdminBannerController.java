package com.sistematecnologico.controller;

import com.sistematecnologico.dto.banner.BannerRequest;
import com.sistematecnologico.dto.banner.BannerResponse;
import com.sistematecnologico.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/banners")
@Tag(name = "Admin Banners", description = "Gestion de banners promocionales (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    @Operation(summary = "Lista todos los banners incluidos los inactivos (ADMIN)")
    public ResponseEntity<List<BannerResponse>> listar() {
        return ResponseEntity.ok(bannerService.listarTodos());
    }

    @PostMapping
    @Operation(summary = "Crea un banner (ADMIN)")
    public ResponseEntity<BannerResponse> crear(@Valid @RequestBody BannerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un banner (ADMIN)")
    public ResponseEntity<BannerResponse> actualizar(@PathVariable Long id,
                                                     @Valid @RequestBody BannerRequest request) {
        return ResponseEntity.ok(bannerService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un banner (ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        bannerService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
