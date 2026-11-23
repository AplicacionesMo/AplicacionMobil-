package cl.duoc.tacticaldev.usuarios.repository;

import cl.duoc.tacticaldev.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data genera el SQL a partir del nombre del metodo. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
