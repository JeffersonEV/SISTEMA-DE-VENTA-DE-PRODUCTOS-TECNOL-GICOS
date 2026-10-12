package com.sistematecnologico.service;

import com.sistematecnologico.dto.categoria.CategoriaRequest;
import com.sistematecnologico.dto.categoria.CategoriaResponse;
import com.sistematecnologico.entity.Categoria;
import com.sistematecnologico.exception.ConflictoException;
import com.sistematecnologico.exception.RecursoNoEncontradoException;
import com.sistematecnologico.exception.ReglaNegocioException;
import com.sistematecnologico.repository.CategoriaRepository;
import com.sistematecnologico.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return toResponse(entidad(id));
    }

    @Transactional(readOnly = true)
    public Categoria entidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada con id " + id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new ConflictoException("Ya existe una categoria con el nombre " + request.nombre());
        }
        Categoria categoria = Categoria.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = entidad(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)) {
            throw new ConflictoException("Ya existe una categoria con el nombre " + request.nombre());
        }
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = entidad(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ReglaNegocioException("No se puede eliminar la categoria porque tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion());
    }
}
