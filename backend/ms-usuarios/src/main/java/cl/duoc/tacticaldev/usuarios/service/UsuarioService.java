package cl.duoc.tacticaldev.usuarios.service;

import cl.duoc.tacticaldev.usuarios.dto.LoginRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioResponse;
import cl.duoc.tacticaldev.usuarios.exception.ApiException;
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

    public UsuarioResponse crear(UsuarioRequest request) {
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

    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);
        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(request.correo().trim().toLowerCase());
        usuario.setRol(request.rol());
        if (request.clave() != null && !request.clave().isBlank()) {
            usuario.setClave(encoder.encode(request.clave()));
        }
        return UsuarioResponse.desde(repository.save(usuario));
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
