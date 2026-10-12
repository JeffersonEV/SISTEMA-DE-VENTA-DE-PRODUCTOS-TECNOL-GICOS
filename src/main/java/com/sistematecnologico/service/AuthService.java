package com.sistematecnologico.service;

import com.sistematecnologico.dto.auth.AuthResponse;
import com.sistematecnologico.dto.auth.LoginRequest;
import com.sistematecnologico.dto.auth.RegistroRequest;
import com.sistematecnologico.entity.Usuario;
import com.sistematecnologico.exception.AutenticacionException;
import com.sistematecnologico.repository.UsuarioRepository;
import com.sistematecnologico.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioService usuarioService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        var usuarioResponse = usuarioService.registrarCliente(request);
        Usuario usuario = usuarioService.porEmail(usuarioResponse.email());
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(request.email());
        if (opt.isEmpty()) {
            throw new AutenticacionException("Credenciales invalidas");
        }
        Usuario usuario = opt.get();
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new AutenticacionException("Credenciales invalidas");
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AutenticacionException("El usuario esta inactivo. Contacte al administrador.");
        }
        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
