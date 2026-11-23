package cl.duoc.tacticaldev.tickets.repository;

import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import cl.duoc.tacticaldev.tickets.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, String> {

    List<Ticket> findAllByOrderByNumeroDesc();

    List<Ticket> findByEstadoOrderByNumeroDesc(EstadoTicket estado);

    /** Para el numero correlativo: el mayor numero usado, o 0 si no hay tickets. */
    @Query("select coalesce(max(t.numero), 0) from Ticket t")
    int ultimoNumero();
}
