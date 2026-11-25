package cl.duoc.tacticaldev.tickets.service;

import cl.duoc.tacticaldev.tickets.client.GuiasClient;
import cl.duoc.tacticaldev.tickets.dto.CambioEstadoRequest;
import cl.duoc.tacticaldev.tickets.dto.CambioEstadoResponse;
import cl.duoc.tacticaldev.tickets.dto.TicketRequest;
import cl.duoc.tacticaldev.tickets.dto.TicketResponse;
import cl.duoc.tacticaldev.tickets.exception.ApiException;
import cl.duoc.tacticaldev.tickets.model.CambioEstado;
import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import cl.duoc.tacticaldev.tickets.model.Ticket;
import cl.duoc.tacticaldev.tickets.repository.CambioEstadoRepository;
import cl.duoc.tacticaldev.tickets.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio de los tickets. */
@Service
public class TicketService {

    public static final String ROL_COORDINADOR = "COORDINADOR";
    public static final String ROL_ADMIN = "ADMIN";

    private final TicketRepository ticketRepository;
    private final CambioEstadoRepository cambioEstadoRepository;
    private final EquipoService equipoService;
    private final GuiasClient guiasClient;

    public TicketService(TicketRepository ticketRepository,
                         CambioEstadoRepository cambioEstadoRepository,
                         EquipoService equipoService,
                         GuiasClient guiasClient) {
        this.ticketRepository = ticketRepository;
        this.cambioEstadoRepository = cambioEstadoRepository;
        this.equipoService = equipoService;
        this.guiasClient = guiasClient;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listar(EstadoTicket estado) {
        List<Ticket> tickets = estado == null
                ? ticketRepository.findAllByOrderByNumeroDesc()
                : ticketRepository.findByEstadoOrderByNumeroDesc(estado);
        return tickets.stream().map(TicketResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse obtener(String id) {
        return TicketResponse.desde(buscar(id));
    }

    /** El tecnico no crea tickets: los crea el coordinador o el administrador. */
    @Transactional
    public TicketResponse crear(TicketRequest request, String rol) {
        if (!esGestor(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el coordinador o el administrador pueden crear tickets");
        }
        Ticket ticket = new Ticket();
        // Numero correlativo: lo asigna el servidor para que nunca se repita entre dispositivos
        ticket.setNumero(ticketRepository.ultimoNumero() + 1);
        ticket.setEstado(EstadoTicket.ABIERTO);
        copiarDatos(request, ticket);
        return TicketResponse.desde(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse actualizar(String id, TicketRequest request) {
        Ticket ticket = buscar(id);
        exigirEditable(ticket);
        copiarDatos(request, ticket);
        return TicketResponse.desde(ticketRepository.save(ticket));
    }

    /**
     * Cambia el estado y deja registro en el historial.
     * Reglas: no se modifica un ticket final, solo el coordinador cancela,
     * y no se cierra un ticket sin al menos una guia (se pregunta a ms-guias).
     */
    @Transactional
    public TicketResponse cambiarEstado(String id, CambioEstadoRequest request, String rol) {
        Ticket ticket = buscar(id);
        exigirEditable(ticket);
        EstadoTicket nuevo = request.estadoNuevo();

        if (nuevo == ticket.getEstado()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El ticket ya esta en ese estado");
        }
        if (nuevo == EstadoTicket.CANCELADO && !esGestor(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el coordinador o el administrador pueden cancelar un ticket");
        }
        if (nuevo == EstadoTicket.CERRADO && guiasClient.contarGuiasDeTicket(id) == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Para cerrar el ticket debes emitir al menos una guia");
        }

        String comentario = request.comentario().trim();
        CambioEstado cambio = new CambioEstado(ticket, ticket.getEstado(), nuevo, comentario,
                request.urisDeFotos(), request.latitud(), request.longitud());
        ticket.getHistorial().add(cambio);
        ticket.setEstado(nuevo);

        if (nuevo == EstadoTicket.CERRADO) {
            ticket.setFechaCierre(LocalDateTime.now());
            ticket.setSolucion(comentario);
        } else if (nuevo == EstadoTicket.CANCELADO) {
            ticket.setFechaCierre(LocalDateTime.now());
            ticket.setMotivoCancelacion(comentario);
        }
        return TicketResponse.desde(ticketRepository.save(ticket));
    }

    /** Solo el coordinador o el administrador eliminan, y solo tickets sin guias emitidas. */
    @Transactional
    public void eliminar(String id, String rol) {
        Ticket ticket = buscar(id);
        if (!esGestor(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el coordinador o el administrador pueden eliminar un ticket");
        }
        if (guiasClient.contarGuiasDeTicket(id) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede eliminar un ticket que tiene guias emitidas");
        }
        ticketRepository.delete(ticket);
    }

    @Transactional(readOnly = true)
    public List<CambioEstadoResponse> historial(String ticketId) {
        buscar(ticketId);
        return cambioEstadoRepository.findByTicket_IdOrderByFechaDesc(ticketId).stream()
                .map(CambioEstadoResponse::desde)
                .toList();
    }

    // ---------- Apoyo ----------

    /** Coordinador y administrador gestionan tickets; el tecnico solo trabaja en ellos. */
    private boolean esGestor(String rol) {
        return ROL_COORDINADOR.equalsIgnoreCase(rol) || ROL_ADMIN.equalsIgnoreCase(rol);
    }

    private void copiarDatos(TicketRequest request, Ticket ticket) {
        ticket.setTitulo(request.titulo().trim());
        ticket.setDescripcion(request.descripcion().trim());
        ticket.setCliente(request.cliente().trim());
        ticket.setDireccion(request.direccion().trim());
        ticket.setPrioridad(request.prioridad());
        ticket.setEquipo(equipoService.obtenerOCrear(request.equipo()));
    }

    private void exigirEditable(Ticket ticket) {
        if (ticket.getEstado().esFinal()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El ticket esta " + ticket.getEstado().name().toLowerCase() + " y no se puede modificar");
        }
    }

    private Ticket buscar(String id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));
    }
}
