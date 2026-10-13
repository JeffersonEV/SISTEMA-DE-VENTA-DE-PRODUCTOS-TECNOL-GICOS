package com.sistematecnologico.service;

import com.sistematecnologico.dto.configuracion.ConfiguracionRequest;
import com.sistematecnologico.dto.configuracion.ConfiguracionResponse;
import com.sistematecnologico.entity.Configuracion;
import com.sistematecnologico.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionService {

    private static final Long ID_UNICO = 1L;

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @Transactional(readOnly = true)
    public ConfiguracionResponse obtener() {
        return configuracionRepository.findById(ID_UNICO)
                .map(this::toResponse)
                .orElseGet(() -> toResponse(Configuracion.builder().id(ID_UNICO).nombreTienda("TechStore").build()));
    }

    @Transactional
    public ConfiguracionResponse actualizar(ConfiguracionRequest request) {
        Configuracion configuracion = configuracionRepository.findById(ID_UNICO)
                .orElseGet(() -> Configuracion.builder().id(ID_UNICO).build());
        configuracion.setNombreTienda(request.nombreTienda());
        configuracion.setDescripcion(request.descripcion());
        configuracion.setEmailContacto(request.emailContacto());
        configuracion.setTelefono(request.telefono());
        configuracion.setWhatsapp(request.whatsapp());
        configuracion.setDireccion(request.direccion());
        configuracion.setFacebookUrl(request.facebookUrl());
        configuracion.setInstagramUrl(request.instagramUrl());
        configuracion.setTwitterUrl(request.twitterUrl());
        return toResponse(configuracionRepository.save(configuracion));
    }

    private ConfiguracionResponse toResponse(Configuracion c) {
        return new ConfiguracionResponse(
                c.getId(),
                c.getNombreTienda(),
                c.getDescripcion(),
                c.getEmailContacto(),
                c.getTelefono(),
                c.getWhatsapp(),
                c.getDireccion(),
                c.getFacebookUrl(),
                c.getInstagramUrl(),
                c.getTwitterUrl()
        );
    }
}
