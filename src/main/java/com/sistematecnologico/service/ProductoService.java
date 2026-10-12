package com.sistematecnologico.service;

import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.producto.ProductoRequest;
import com.sistematecnologico.dto.producto.ProductoResponse;
import com.sistematecnologico.entity.Categoria;
import com.sistematecnologico.entity.Producto;
import com.sistematecnologico.exception.RecursoNoEncontradoException;
import com.sistematecnologico.exception.ReglaNegocioException;
import com.sistematecnologico.repository.DetallePedidoRepository;
import com.sistematecnologico.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final DetallePedidoRepository detallePedidoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaService categoriaService,
                           DetallePedidoRepository detallePedidoRepository) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listar(String nombre, Long categoriaId,
                                                 BigDecimal minPrecio, BigDecimal maxPrecio,
                                                 Boolean soloActivos, Pageable pageable) {
        Boolean activo = Boolean.TRUE.equals(soloActivos) ? Boolean.TRUE : null;
        Page<Producto> page = productoRepository.buscarConFiltros(
                vacio(nombre), categoriaId, minPrecio, maxPrecio, activo, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return toResponse(entidad(id));
    }

    @Transactional(readOnly = true)
    public Producto entidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = categoriaService.entidad(request.categoriaId());
        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio(request.precio())
                .stock(request.stock())
                .marca(request.marca())
                .imagenUrl(request.imagenUrl())
                .activo(request.activo() == null || request.activo())
                .categoria(categoria)
                .build();
        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = entidad(id);
        Categoria categoria = categoriaService.entidad(request.categoriaId());
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setMarca(request.marca());
        producto.setImagenUrl(request.imagenUrl());
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
        producto.setCategoria(categoria);
        return toResponse(productoRepository.save(producto));
    }

    /**
     * Si el producto tiene pedidos asociados se aplica borrado logico (activo=false);
     * en caso contrario se elimina fisicamente.
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = entidad(id);
        if (detallePedidoRepository.existsByProductoId(id)) {
            producto.setActivo(false);
            productoRepository.save(producto);
            return;
        }
        productoRepository.delete(producto);
    }

    @Transactional(readOnly = true)
    public List<Producto> stockBajo(int umbral) {
        if (umbral < 0) {
            throw new ReglaNegocioException("El umbral no puede ser negativo");
        }
        return productoRepository.productosStockBajo(umbral);
    }

    private String vacio(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    public ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock(),
                p.getMarca(), p.getImagenUrl(), p.getActivo(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre() : null);
    }
}
