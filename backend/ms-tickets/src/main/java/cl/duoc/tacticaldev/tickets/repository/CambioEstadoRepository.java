package cl.duoc.tacticaldev.tickets.repository;

import cl.duoc.tacticaldev.tickets.model.CambioEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CambioEstadoRepository extends JpaRepository<CambioEstado, String> {

    /** "Ticket_Id" navega la relacion: cambios cuyo ticket tiene ese id. */
    List<CambioEstado> findByTicket_IdOrderByFechaDesc(String ticketId);
}
