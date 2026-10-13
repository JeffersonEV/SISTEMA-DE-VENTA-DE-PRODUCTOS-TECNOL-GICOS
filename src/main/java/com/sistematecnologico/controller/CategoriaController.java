package com.sistematecnologico.controller;

import com.sistematecnologico.dto.categoria.CategoriaRequest;
import com.sistematecnologico.dto.categoria.CategoriaResponse;
import com.sistematecnologico.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gestion de categorias (escritura solo ADMIN)")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Lista todas las categorias (publico)")
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/arbol")
    @Operation(summary = "Lista categorias con sus subcategorias anidadas (publico)")
    public ResponseEntity<List<CategoriaResponse>> arbol() {
        return ResponseEntity.ok(categoriaService.arbol());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoria por id (publico)")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crea una categoria (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoria (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una categoria sin productos (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
