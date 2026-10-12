package com.sistematecnologico.repository;

import com.sistematecnologico.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ---------- Derived queries ----------
    boolean existsByCategoriaId(Long categoriaId);

    Page<Producto> findByActivoTrue(Pageable pageable);

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    // ---------- JPQL ----------

    /**
     * Busqueda paginada con filtros opcionales de catalogo.
     * Cualquier parametro null se ignora.
     */
    @Query("""
            SELECT p FROM Producto p
            WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
              AND (:minPrecio IS NULL OR p.precio >= :minPrecio)
              AND (:maxPrecio IS NULL OR p.precio <= :maxPrecio)
              AND (:activo IS NULL OR p.activo = :activo)
            """)
    Page<Producto> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("categoriaId") Long categoriaId,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio,
            @Param("activo") Boolean activo,
            Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= :umbral ORDER BY p.stock ASC")
    List<Producto> productosStockBajo(@Param("umbral") int umbral);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.precio BETWEEN :min AND :max ORDER BY p.precio ASC")
    List<Producto> productosEnRangoPrecio(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true")
    long contarActivos();
}
