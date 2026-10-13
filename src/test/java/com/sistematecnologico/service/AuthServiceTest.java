package com.sistematecnologico.service;

import com.sistematecnologico.dto.auth.AuthResponse;
import com.sistematecnologico.dto.auth.LoginRequest;
import com.sistematecnologico.dto.auth.RegistroRequest;
import com.sistematecnologico.dto.usuario.UsuarioResponse;
import com.sistematecnologico.entity.Rol;
import com.sistematecnologico.entity.Usuario;
import com.sistematecnologico.exception.AutenticacionException;
import com.sistematecnologico.repository.UsuarioRepository;
import com.sistematecnologico.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario(String email, boolean activo) {
        return Usuario.builder()
                .id(1L).nombre("Juan").apellido("Perez")
                .email(email).password("$2a$10$hash").rol(Rol.CLIENTE).activo(activo)
                .build();
    }

    @Test
    @DisplayName("login(): devuelve AuthResponse con token si las credenciales son validas")
    void loginOk() {
        Usuario u = usuario("juan@test.com", true);
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("clave123", u.getPassword())).thenReturn(true);
        when(jwtService.generarToken(u)).thenReturn("jwt-token");

        AuthResponse r = authService.login(new LoginRequest("juan@test.com", "clave123"));

        assertEquals("jwt-token", r.token());
        assertEquals("Bearer", r.tipo());
        assertEquals(Rol.CLIENTE, r.rol());
        assertEquals("juan@test.com", r.email());
    }

    @Test
    @DisplayName("login(): password incorrecta lanza AutenticacionException")
    void loginPasswordIncorrecta() {
        Usuario u = usuario("juan@test.com", true);
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", u.getPassword())).thenReturn(false);

        assertThrows(AutenticacionException.class,
                () -> authService.login(new LoginRequest("juan@test.com", "mala")));
    }

    @Test
    @DisplayName("login(): usuario inactivo no puede iniciar sesion")
    void loginUsuarioInactivo() {
        Usuario u = usuario("juan@test.com", false);
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("clave123", u.getPassword())).thenReturn(true);

        assertThrows(AutenticacionException.class,
                () -> authService.login(new LoginRequest("juan@test.com", "clave123")));
    }

    @Test
    @DisplayName("login(): email no existente lanza AutenticacionException")
    void loginEmailInexistente() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThrows(AutenticacionException.class,
                () -> authService.login(new LoginRequest("nadie@test.com", "clave123")));
    }

    @Test
    @DisplayName("registrar(): crea cliente y devuelve token")
    void registrarOk() {
        RegistroRequest req = new RegistroRequest("Juan", "Perez", "juan@test.com", "clave123");
        UsuarioResponse resp = new UsuarioResponse(1L, "Juan", "Perez", "juan@test.com", true, Rol.CLIENTE, null);
        Usuario u = usuario("juan@test.com", true);

        when(usuarioService.registrarCliente(req)).thenReturn(resp);
        when(usuarioService.porEmail("juan@test.com")).thenReturn(u);
        when(jwtService.generarToken(u)).thenReturn("jwt-token");

        AuthResponse r = authService.registrar(req);

        assertEquals("jwt-token", r.token());
        assertEquals(1L, r.id());
        verify(usuarioService).registrarCliente(req);
    }
}
