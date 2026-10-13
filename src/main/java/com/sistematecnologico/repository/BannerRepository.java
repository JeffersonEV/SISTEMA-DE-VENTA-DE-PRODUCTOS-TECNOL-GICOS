package com.sistematecnologico.repository;

import com.sistematecnologico.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByActivoTrueOrderByOrdenAscIdAsc();

    List<Banner> findAllByOrderByOrdenAscIdAsc();
}
