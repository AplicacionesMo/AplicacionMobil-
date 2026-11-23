package cl.duoc.tacticaldev.usuarios.service;

import cl.duoc.tacticaldev.usuarios.dto.LoginRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioResponse;
import cl.duoc.tacticaldev.usuarios.exception.ApiException;
import cl.duoc.tacticaldev.usuarios.model.Rol;
import cl.duoc.tacticaldev.usuarios.model.Usuario;
import cl.duoc.tacticaldev.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Se prueba la logica del servicio sin base de datos:
 * el repositorio es un mock de Mockito y el encriptador es el real.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, encoder);
    }

    private Usuario tecnico() {
        Usuario u = new Usuario("Tecnico", "tecnico@tacticaldev.cl", encoder.encode("tecnico123"), Rol.TECNICO);
        u.setId(1L);
        return u;
    }

    @Test
    void loginCorrectoDevuelveElUsuarioConSuRol() {
        when(repository.findByCorreoIgnoreCase("tecnico@tacticaldev.cl")).thenReturn(Optional.of(tecnico()));

        UsuarioResponse respuesta = service.login(new LoginRequest("tecnico@tacticaldev.cl", "tecnico123"));

        assertEquals(Rol.TECNICO, respuesta.rol());
        assertEquals("tecnico@tacticaldev.cl", respuesta.correo());
    }

    @Test
    void loginConClaveIncorrectaLanza401() {
        when(repository.findByCorreoIgnoreCase("tecnico@tacticaldev.cl")).thenReturn(Optional.of(tecnico()));

        ApiException e = assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("tecnico@tacticaldev.cl", "otra")));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getEstado());
    }

    @Test
    void loginDeUsuarioInactivoLanza401() {
        Usuario inactivo = tecnico();
        inactivo.setActivo(false);
        when(repository.findByCorreoIgnoreCase("tecnico@tacticaldev.cl")).thenReturn(Optional.of(inactivo));

        assertThrows(ApiException.class,
                () -> service.login(new LoginRequest("tecnico@tacticaldev.cl", "tecnico123")));
    }

    @Test
    void crearGuardaLaClaveEncriptada() {
        when(repository.existsByCorreoIgnoreCase("nuevo@tacticaldev.cl")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crear(new UsuarioRequest("Nuevo", "nuevo@tacticaldev.cl", "secreta1", Rol.TECNICO));

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(u ->
                !u.getClave().equals("secreta1") && encoder.matches("secreta1", u.getClave())));
    }

    @Test
    void crearConCorreoRepetidoLanza409() {
        when(repository.existsByCorreoIgnoreCase("tecnico@tacticaldev.cl")).thenReturn(true);

        ApiException e = assertThrows(ApiException.class, () ->
                service.crear(new UsuarioRequest("X", "tecnico@tacticaldev.cl", "secreta1", Rol.TECNICO)));

        assertEquals(HttpStatus.CONFLICT, e.getEstado());
        verify(repository, never()).save(any());
    }

    @Test
    void crearConClaveCortaLanza400() {
        ApiException e = assertThrows(ApiException.class, () ->
                service.crear(new UsuarioRequest("X", "x@tacticaldev.cl", "123", Rol.TECNICO)));

        assertEquals(HttpStatus.BAD_REQUEST, e.getEstado());
    }

    @Test
    void actualizarSinClaveConservaLaAnterior() {
        Usuario existente = tecnico();
        String hashAnterior = existente.getClave();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse r = service.actualizar(1L,
                new UsuarioRequest("Nombre Nuevo", "tecnico@tacticaldev.cl", "", Rol.COORDINADOR));

        assertEquals("Nombre Nuevo", r.nombre());
        assertEquals(Rol.COORDINADOR, r.rol());
        assertEquals(hashAnterior, existente.getClave());
    }

    @Test
    void actualizarConClaveNuevaLaEncripta() {
        Usuario existente = tecnico();
        String hashAnterior = existente.getClave();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        service.actualizar(1L, new UsuarioRequest("T", "tecnico@tacticaldev.cl", "nueva123", Rol.TECNICO));

        assertNotEquals(hashAnterior, existente.getClave());
    }

    @Test
    void obtenerInexistenteLanza404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, e.getEstado());
    }

    @Test
    void listarConvierteTodosLosUsuarios() {
        when(repository.findAll()).thenReturn(List.of(tecnico()));

        assertEquals(1, service.listar().size());
    }

    @Test
    void eliminarBorraElUsuarioEncontrado() {
        Usuario existente = tecnico();
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repository).delete(existente);
    }
}
