package com.sistematecnologico.controller;

import com.sistematecnologico.dto.reporte.ProductoStockBajo;
import com.sistematecnologico.dto.reporte.ResumenReporte;
import com.sistematecnologico.dto.reporte.TopProducto;
import com.sistematecnologico.dto.reporte.VentasPorCategoria;
import com.sistematecnologico.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@Tag(name = "Admin - Reportes", description = "Reportes y metricas (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminReporteController {

    private final ReporteService reporteService;

    public AdminReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen: total ventas, pedidos por estado, usuarios y productos")
    public ResponseEntity<ResumenReporte> resumen() {
        return ResponseEntity.ok(reporteService.resumen());
    }

    @GetMapping("/top-productos")
    @Operation(summary = "Productos mas vendidos")
    public ResponseEntity<List<TopProducto>> topProductos(
            @RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(reporteService.topProductos(limite));
    }

    @GetMapping("/ventas-por-categoria")
    @Operation(summary = "Ventas agrupadas por categoria")
    public ResponseEntity<List<VentasPorCategoria>> ventasPorCategoria() {
        return ResponseEntity.ok(reporteService.ventasPorCategoria());
    }

    @GetMapping("/stock-bajo")
    @Operation(summary = "Productos con stock menor o igual al umbral")
    public ResponseEntity<List<ProductoStockBajo>> stockBajo(
            @RequestParam(defaultValue = "5") int umbral) {
        return ResponseEntity.ok(reporteService.stockBajo(umbral));
    }
}
