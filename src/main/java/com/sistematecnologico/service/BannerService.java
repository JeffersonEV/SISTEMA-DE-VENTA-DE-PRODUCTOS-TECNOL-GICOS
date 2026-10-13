package com.sistematecnologico.service;

import com.sistematecnologico.dto.banner.BannerRequest;
import com.sistematecnologico.dto.banner.BannerResponse;
import com.sistematecnologico.entity.Banner;
import com.sistematecnologico.exception.RecursoNoEncontradoException;
import com.sistematecnologico.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> listarActivos() {
        return bannerRepository.findByActivoTrueOrderByOrdenAscIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BannerResponse> listarTodos() {
        return bannerRepository.findAllByOrderByOrdenAscIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BannerResponse crear(BannerRequest request) {
        Banner banner = Banner.builder()
                .titulo(request.titulo())
                .subtitulo(request.subtitulo())
                .imagenUrl(request.imagenUrl())
                .enlaceUrl(request.enlaceUrl())
                .textoBoton(request.textoBoton())
                .orden(request.orden() != null ? request.orden() : 0)
                .activo(request.activo() == null || request.activo())
                .build();
        return toResponse(bannerRepository.save(banner));
    }

    @Transactional
    public BannerResponse actualizar(Long id, BannerRequest request) {
        Banner banner = entidad(id);
        banner.setTitulo(request.titulo());
        banner.setSubtitulo(request.subtitulo());
        banner.setImagenUrl(request.imagenUrl());
        banner.setEnlaceUrl(request.enlaceUrl());
        banner.setTextoBoton(request.textoBoton());
        if (request.orden() != null) {
            banner.setOrden(request.orden());
        }
        if (request.activo() != null) {
            banner.setActivo(request.activo());
        }
        return toResponse(bannerRepository.save(banner));
    }

    @Transactional
    public void eliminar(Long id) {
        bannerRepository.delete(entidad(id));
    }

    @Transactional(readOnly = true)
    public Banner entidad(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Banner no encontrado con id " + id));
    }

    private BannerResponse toResponse(Banner b) {
        return new BannerResponse(
                b.getId(),
                b.getTitulo(),
                b.getSubtitulo(),
                b.getImagenUrl(),
                b.getEnlaceUrl(),
                b.getTextoBoton(),
                b.getOrden(),
                b.getActivo()
        );
    }
}
