package com.sistematecnologico.controller;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.producto.ProductoRequest;
import com.sistematecnologico.dto.producto.ProductoResponse;
import com.sistematecnologico.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestion de productos (escritura solo ADMIN)")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Lista productos con paginacion y filtros (publico)")
    public ResponseEntity<PageResponse<ProductoResponse>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio,
            @RequestParam(defaultValue = "true") boolean soloActivos,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(productoService.listar(nombre, categoriaId, minPrecio, maxPrecio, soloActivos, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id (publico)")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crea un producto (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un producto (borrado logico si tiene pedidos) (ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
