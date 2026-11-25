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

    /**
     * El coordinador y el administrador ven todos los tickets.
     * El tecnico ve unicamente los que le asignaron: los que no son suyos, ni siquiera
     * los que estan sin asignar, no salen en la lista.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> listar(EstadoTicket estado, String rol, Long usuarioId) {
        List<Ticket> tickets;
        if (esGestor(rol)) {
            tickets = estado == null
                    ? ticketRepository.findAllByOrderByNumeroDesc()
                    : ticketRepository.findByEstadoOrderByNumeroDesc(estado);
        } else if (usuarioId == null) {
            // Sin identificar no se muestra nada: es preferible una lista vacia a filtrar mal
            tickets = List.of();
        } else {
            tickets = estado == null
                    ? ticketRepository.findByTecnicoIdOrderByNumeroDesc(usuarioId)
                    : ticketRepository.findByTecnicoIdAndEstadoOrderByNumeroDesc(usuarioId, estado);
        }
        return tickets.stream().map(TicketResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse obtener(String id, String rol, Long usuarioId) {
        Ticket ticket = buscar(id);
        exigirAcceso(ticket, rol, usuarioId);
        return TicketResponse.desde(ticket);
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
    public TicketResponse actualizar(String id, TicketRequest request, String rol, Long usuarioId) {
        Ticket ticket = buscar(id);
        exigirAcceso(ticket, rol, usuarioId);
        exigirEditable(ticket);
        copiarDatos(request, ticket);
        return TicketResponse.desde(ticketRepository.save(ticket));
    }

    /**
     * Cambia el estado y deja registro en el historial.
     * Reglas: no se modifica un ticket final, solo el coordinador cancela,
     * y no se finaliza un ticket sin al menos una guia (se pregunta a ms-guias).
     * Abierto, Transito y Cancelado se alcanzan sin guia.
     */
    @Transactional
    public TicketResponse cambiarEstado(String id, CambioEstadoRequest request, String rol, Long usuarioId) {
        Ticket ticket = buscar(id);
        exigirAcceso(ticket, rol, usuarioId);
        EstadoTicket nuevo = request.estadoNuevo();
        boolean reabriendo = ticket.getEstado().esFinal();

        if (reabriendo) {
            exigirReapertura(ticket, nuevo, rol, request.comentario());
        }

        if (nuevo == ticket.getEstado()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El ticket ya esta en ese estado");
        }
        if (nuevo == EstadoTicket.CANCELADO && !esGestor(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el coordinador o el administrador pueden cancelar un ticket");
        }
        if (nuevo == EstadoTicket.FINALIZADO && guiasClient.contarGuiasDeTicket(id) == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Debes crear una guia");
        }

        String comentario = comentarioDe(request, nuevo);
        CambioEstado cambio = new CambioEstado(ticket, ticket.getEstado(), nuevo, comentario,
                request.urisDeFotos(), request.latitud(), request.longitud());
        ticket.getHistorial().add(cambio);
        ticket.setEstado(nuevo);

        if (nuevo == EstadoTicket.FINALIZADO) {
            ticket.setFechaCierre(LocalDateTime.now());
            ticket.setSolucion(comentario);
        } else if (nuevo == EstadoTicket.CANCELADO) {
            ticket.setFechaCierre(LocalDateTime.now());
            ticket.setMotivoCancelacion(comentario);
        } else if (reabriendo) {
            // El ticket vuelve a estar vivo: ya no tiene cierre ni solucion.
            // Lo anterior no se pierde, queda en el historial.
            ticket.setFechaCierre(null);
            ticket.setSolucion(null);
            ticket.setMotivoCancelacion(null);
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

    /**
     * El comentario es opcional: el detalle del trabajo vive en la guia de servicio.
     * Si no viene, se deja una glosa automatica para que el historial igual se entienda.
     */
    private String comentarioDe(CambioEstadoRequest request, EstadoTicket nuevo) {
        String comentario = request.comentario() == null ? "" : request.comentario().trim();
        return comentario.isEmpty() ? "Cambio de estado a " + etiqueta(nuevo) : comentario;
    }

    private String etiqueta(EstadoTicket estado) {
        return switch (estado) {
            case ABIERTO -> "Abierto";
            case TRANSITO -> "En transito";
            case FINALIZADO -> "Finalizado";
            case CANCELADO -> "Cancelado";
        };
    }

    /**
     * Reabrir es excepcional: devuelve a Abierto un ticket ya finalizado o cancelado.
     * Solo lo hace el coordinador o el administrador, y siempre con un motivo, para que
     * el historial explique por que se reabrio y no sirva para tapar un cierre equivocado.
     */
    private void exigirReapertura(Ticket ticket, EstadoTicket nuevo, String rol, String comentario) {
        if (!esGestor(rol)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Solo el coordinador o el administrador pueden reabrir un ticket");
        }
        if (nuevo != EstadoTicket.ABIERTO) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Un ticket " + etiqueta(ticket.getEstado()).toLowerCase() + " solo se puede reabrir");
        }
        if (comentario == null || comentario.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Debes indicar el motivo para reabrir el ticket");
        }
    }

    /**
     * Un tecnico solo entra a los tickets que le asignaron.
     * El coordinador y el administrador entran a todos.
     */
    private void exigirAcceso(Ticket ticket, String rol, Long usuarioId) {
        if (esGestor(rol)) return;
        if (usuarioId != null && usuarioId.equals(ticket.getTecnicoId())) return;
        throw new ApiException(HttpStatus.FORBIDDEN, "Este ticket esta asignado a otro tecnico");
    }

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
        ticket.setTecnicoId(request.tecnicoId());
        ticket.setTecnicoNombre(request.tecnicoNombre() == null ? null : request.tecnicoNombre().trim());
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
