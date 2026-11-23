package cl.duoc.tacticaldev.tickets.repository;

import cl.duoc.tacticaldev.tickets.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, String> {

    Optional<Equipo> findBySerieIgnoreCase(String serie);
}
