package cl.duoc.tacticaldev.usuarios.config;

import cl.duoc.tacticaldev.usuarios.model.Rol;
import cl.duoc.tacticaldev.usuarios.model.Usuario;
import cl.duoc.tacticaldev.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Al iniciar, crea los usuarios de prueba que falten.
 * Se revisa uno por uno (por correo) para que el admin tambien se cree
 * en una base que ya tenia el tecnico y el coordinador.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JdbcTemplate jdbc;

    public DatosIniciales(UsuarioRepository repository, PasswordEncoder encoder, JdbcTemplate jdbc) {
        this.repository = repository;
        this.encoder = encoder;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        corregirColumnaRol();
        crearSiNoExiste("Tecnico de Prueba", "tecnico@tacticaldev.cl", "tecnico123", Rol.TECNICO);
        crearSiNoExiste("Coordinador de Prueba", "coordinador@tacticaldev.cl", "coord123", Rol.COORDINADOR);
        crearSiNoExiste("Administrador", "admin@tacticaldev.cl", "admin123", Rol.ADMIN);
    }

    private void crearSiNoExiste(String nombre, String correo, String clave, Rol rol) {
        if (!repository.existsByCorreoIgnoreCase(correo)) {
            repository.save(new Usuario(nombre, correo, encoder.encode(clave), rol));
        }
    }

    /**
     * Las bases creadas antes de existir ADMIN tienen la columna rol como
     * ENUM('TECNICO','COORDINADOR') y rechazarian el rol nuevo. Se cambia a VARCHAR.
     * En H2 o en una base nueva no hace falta: si falla, se ignora.
     */
    private void corregirColumnaRol() {
        try {
            jdbc.execute("ALTER TABLE usuarios MODIFY COLUMN rol VARCHAR(20) NOT NULL");
        } catch (Exception e) {
            log.debug("No fue necesario corregir la columna rol: {}", e.getMessage());
        }
    }
}
