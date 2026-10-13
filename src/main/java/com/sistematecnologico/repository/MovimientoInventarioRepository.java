package com.sistematecnologico.repository;

import com.sistematecnologico.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    Page<MovimientoInventario> findAllByOrderByFechaDescIdDesc(Pageable pageable);

    List<MovimientoInventario> findByProductoIdOrderByFechaDescIdDesc(Long productoId);
}
