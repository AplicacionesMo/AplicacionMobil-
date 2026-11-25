package cl.duoc.tacticaldev.usuarios.service;

import cl.duoc.tacticaldev.usuarios.dto.LoginRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioResponse;
import cl.duoc.tacticaldev.usuarios.exception.ApiException;
import cl.duoc.tacticaldev.usuarios.model.Rol;
import cl.duoc.tacticaldev.usuarios.model.Usuario;
import cl.duoc.tacticaldev.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/** Toda la logica de negocio de usuarios. El controlador solo recibe y responde. */
@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = repository.findByCorreoIgnoreCase(request.correo().trim())
                .filter(Usuario::isActivo)
                .filter(u -> encoder.matches(request.clave(), u.getClave()))
                // Mismo mensaje para correo inexistente o clave mala: no se revela cual fallo
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        return UsuarioResponse.desde(usuario);
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    /** Solo el administrador crea usuarios (el rol llega en el header X-Rol). */
    public UsuarioResponse crear(UsuarioRequest request, String rolSolicitante) {
        exigirAdmin(rolSolicitante, "Solo el administrador puede crear usuarios");
        if (request.clave() == null || request.clave().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La clave debe tener al menos 6 caracteres");
        }
        if (repository.existsByCorreoIgnoreCase(request.correo())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        Usuario nuevo = new Usuario(
                request.nombre().trim(),
                request.correo().trim().toLowerCase(),
                encoder.encode(request.clave()),
                request.rol()
        );
        return UsuarioResponse.desde(repository.save(nuevo));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request, String rolSolicitante) {
        exigirAdmin(rolSolicitante, "Solo el administrador puede editar usuarios");
        Usuario usuario = buscar(id);
        if (usuario.getRol() == Rol.ADMIN && request.rol() != Rol.ADMIN && esUltimoAdmin()) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede quitar el rol al ultimo administrador");
        }
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(request.correo().trim().toLowerCase());
        usuario.setRol(request.rol());
        if (request.clave() != null && !request.clave().isBlank()) {
            usuario.setClave(encoder.encode(request.clave()));
        }
        return UsuarioResponse.desde(repository.save(usuario));
    }

    /** Solo el administrador elimina, y nunca al ultimo administrador. */
    public void eliminar(Long id, String rolSolicitante) {
        exigirAdmin(rolSolicitante, "Solo el administrador puede eliminar usuarios");
        Usuario usuario = buscar(id);
        if (usuario.getRol() == Rol.ADMIN && esUltimoAdmin()) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede eliminar al ultimo administrador");
        }
        repository.delete(usuario);
    }

    private void exigirAdmin(String rolSolicitante, String mensaje) {
        if (!Rol.ADMIN.name().equalsIgnoreCase(rolSolicitante)) {
            throw new ApiException(HttpStatus.FORBIDDEN, mensaje);
        }
    }

    private boolean esUltimoAdmin() {
        return repository.countByRol(Rol.ADMIN) <= 1;
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
