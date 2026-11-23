package cl.duoc.tacticaldev.guias.repository;

import cl.duoc.tacticaldev.guias.model.Guia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GuiaRepository extends JpaRepository<Guia, String> {

    List<Guia> findByTicketIdOrderByNumeroDesc(String ticketId);

    List<Guia> findAllByOrderByNumeroDesc();

    long countByTicketId(String ticketId);

    @Query("select coalesce(max(g.numero), 0) from Guia g")
    int ultimoNumero();
}
