package cl.duoc.tacticaldev.usuarios.config;

import cl.duoc.tacticaldev.usuarios.model.Rol;
import cl.duoc.tacticaldev.usuarios.model.Usuario;
import cl.duoc.tacticaldev.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Al iniciar por primera vez, crea los usuarios de prueba. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Usuario("Tecnico de Prueba", "tecnico@tacticaldev.cl",
                encoder.encode("tecnico123"), Rol.TECNICO));
        repository.save(new Usuario("Coordinador de Prueba", "coordinador@tacticaldev.cl",
                encoder.encode("coord123"), Rol.COORDINADOR));
    }
}
