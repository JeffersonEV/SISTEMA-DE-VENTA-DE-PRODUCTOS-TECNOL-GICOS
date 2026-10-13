package com.sistematecnologico.controller;

import com.sistematecnologico.dto.banner.BannerResponse;
import com.sistematecnologico.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@Tag(name = "Banners", description = "Banners promocionales activos (publico)")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    @Operation(summary = "Lista los banners activos ordenados (publico)")
    public ResponseEntity<List<BannerResponse>> listar() {
        return ResponseEntity.ok(bannerService.listarActivos());
    }
}
