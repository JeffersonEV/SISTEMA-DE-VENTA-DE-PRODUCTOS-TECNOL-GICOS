package com.sistematecnologico.service;

import com.sistematecnologico.dto.auth.RegistroRequest;
import com.sistematecnologico.dto.common.PageResponse;
import com.sistematecnologico.dto.usuario.RolUpdateRequest;
import com.sistematecnologico.dto.usuario.UsuarioRequest;
import com.sistematecnologico.dto.usuario.UsuarioResponse;
import com.sistematecnologico.dto.usuario.UsuarioUpdateRequest;
import com.sistematecnologico.entity.Rol;
import com.sistematecnologico.entity.Usuario;
import com.sistematecnologico.exception.ConflictoException;
import com.sistematecnologico.exception.RecursoNoEncontradoException;
import com.sistematecnologico.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listarPaginado(Pageable pageable) {
        Page<Usuario> page = usuarioRepository.findAll(pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return toResponse(entidad(id));
    }

    @Transactional(readOnly = true)
    public Usuario entidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public Usuario porEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioResponse registrarCliente(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoException("Ya existe un usuario registrado con el email " + request.email());
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoException("Ya existe un usuario registrado con el email " + request.email());
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(request.rol())
                .activo(true)
                .build();
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = entidad(id);
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictoException("El email " + request.email() + " ya esta en uso");
        }
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarActivo(Long id, boolean activo) {
        Usuario usuario = entidad(id);
        usuario.setActivo(activo);
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarRol(Long id, RolUpdateRequest request) {
        Usuario usuario = entidad(id);
        usuario.setRol(request.rol());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = entidad(id);
        usuarioRepository.delete(usuario);
    }

    public UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getApellido(), u.getEmail(),
                u.getActivo(), u.getRol(), u.getFechaRegistro());
    }
}
