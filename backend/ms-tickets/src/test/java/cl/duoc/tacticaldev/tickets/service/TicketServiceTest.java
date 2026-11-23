package cl.duoc.tacticaldev.tickets.service;

import cl.duoc.tacticaldev.tickets.client.GuiasClient;
import cl.duoc.tacticaldev.tickets.dto.CambioEstadoRequest;
import cl.duoc.tacticaldev.tickets.dto.EquipoRequest;
import cl.duoc.tacticaldev.tickets.dto.TicketRequest;
import cl.duoc.tacticaldev.tickets.dto.TicketResponse;
import cl.duoc.tacticaldev.tickets.exception.ApiException;
import cl.duoc.tacticaldev.tickets.model.CambioEstado;
import cl.duoc.tacticaldev.tickets.model.Equipo;
import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import cl.duoc.tacticaldev.tickets.model.Prioridad;
import cl.duoc.tacticaldev.tickets.model.Propiedad;
import cl.duoc.tacticaldev.tickets.model.Ticket;
import cl.duoc.tacticaldev.tickets.repository.CambioEstadoRepository;
import cl.duoc.tacticaldev.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reglas de negocio de los tickets. Repositorios, servicio de equipos y el cliente
 * de ms-guias son mocks: la prueba no necesita MySQL ni los otros microservicios.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private CambioEstadoRepository cambioEstadoRepository;
    @Mock private EquipoService equipoService;
    @Mock private GuiasClient guiasClient;

    @InjectMocks private TicketService service;

    private Equipo equipo;

    @BeforeEach
    void setUp() {
        equipo = new Equipo("Split muro", "LG 12000", "ABC123", Propiedad.PROPIO_DEL_CLIENTE);
        equipo.setId("eq-1");
    }

    private Ticket ticket(EstadoTicket estado) {
        Ticket t = new Ticket();
        t.setId("t-1");
        t.setNumero(1);
        t.setTitulo("No enfria");
        t.setDescripcion("El equipo no enfria la sala");
        t.setCliente("Banco Uno");
        t.setDireccion("Av. Siempre Viva 123");
        t.setPrioridad(Prioridad.ALTA);
        t.setEstado(estado);
        t.setEquipo(equipo);
        return t;
    }

    private TicketRequest request() {
        return new TicketRequest("No enfria", "El equipo no enfria la sala", "Banco Uno",
                "Av. Siempre Viva 123", Prioridad.ALTA,
                new EquipoRequest("Split muro", "LG 12000", "abc123", Propiedad.PROPIO_DEL_CLIENTE));
    }

    private CambioEstadoRequest cambio(EstadoTicket nuevo) {
        return new CambioEstadoRequest(nuevo, "Comentario de prueba", List.of(), -33.45, -70.66);
    }

    @Test
    void crearAsignaElSiguienteNumeroYEstadoAbierto() {
        when(ticketRepository.ultimoNumero()).thenReturn(7);
        when(equipoService.obtenerOCrear(any())).thenReturn(equipo);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse r = service.crear(request());

        assertEquals(8, r.numero());
        assertEquals(EstadoTicket.ABIERTO, r.estado());
        assertEquals("ABC123", r.equipo().serie());
    }

    @Test
    void actualizarTicketCerradoLanza409() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.CERRADO)));

        ApiException e = assertThrows(ApiException.class, () -> service.actualizar("t-1", request()));

        assertEquals(HttpStatus.CONFLICT, e.getEstado());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void actualizarTicketAbiertoGuardaLosCambios() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.ABIERTO)));
        when(equipoService.obtenerOCrear(any())).thenReturn(equipo);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse r = service.actualizar("t-1", request());

        assertEquals("Banco Uno", r.cliente());
    }

    @Test
    void cambiarEstadoRegistraElHistorial() {
        Ticket t = ticket(EstadoTicket.ABIERTO);
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(t));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse r = service.cambiarEstado("t-1", cambio(EstadoTicket.TRANSITO), "TECNICO");

        assertEquals(EstadoTicket.TRANSITO, r.estado());
        assertEquals(1, t.getHistorial().size());
        CambioEstado registro = t.getHistorial().get(0);
        assertEquals(EstadoTicket.ABIERTO, registro.getEstadoAnterior());
        assertEquals(EstadoTicket.TRANSITO, registro.getEstadoNuevo());
    }

    @Test
    void cambiarAlMismoEstadoLanza400() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.ABIERTO)));

        ApiException e = assertThrows(ApiException.class,
                () -> service.cambiarEstado("t-1", cambio(EstadoTicket.ABIERTO), "TECNICO"));

        assertEquals(HttpStatus.BAD_REQUEST, e.getEstado());
    }

    @Test
    void cerrarSinGuiasLanza409() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.TRABAJO_EN_PROCESO)));
        when(guiasClient.contarGuiasDeTicket("t-1")).thenReturn(0L);

        ApiException e = assertThrows(ApiException.class,
                () -> service.cambiarEstado("t-1", cambio(EstadoTicket.CERRADO), "TECNICO"));

        assertEquals(HttpStatus.CONFLICT, e.getEstado());
    }

    @Test
    void cerrarConGuiaGuardaSolucionYFecha() {
        Ticket t = ticket(EstadoTicket.TRABAJO_EN_PROCESO);
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(t));
        when(guiasClient.contarGuiasDeTicket("t-1")).thenReturn(1L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse r = service.cambiarEstado("t-1", cambio(EstadoTicket.CERRADO), "TECNICO");

        assertEquals(EstadoTicket.CERRADO, r.estado());
        assertEquals("Comentario de prueba", r.solucion());
        assertNotNull(r.fechaCierre());
    }

    @Test
    void tecnicoNoPuedeCancelar() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.ABIERTO)));

        ApiException e = assertThrows(ApiException.class,
                () -> service.cambiarEstado("t-1", cambio(EstadoTicket.CANCELADO), "TECNICO"));

        assertEquals(HttpStatus.FORBIDDEN, e.getEstado());
    }

    @Test
    void coordinadorCancelaYQuedaElMotivo() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.ABIERTO)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse r = service.cambiarEstado("t-1", cambio(EstadoTicket.CANCELADO), "COORDINADOR");

        assertEquals(EstadoTicket.CANCELADO, r.estado());
        assertEquals("Comentario de prueba", r.motivoCancelacion());
    }

    @Test
    void eliminarComoTecnicoLanza403() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.ABIERTO)));

        ApiException e = assertThrows(ApiException.class, () -> service.eliminar("t-1", "TECNICO"));

        assertEquals(HttpStatus.FORBIDDEN, e.getEstado());
    }

    @Test
    void eliminarConGuiasLanza409() {
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(ticket(EstadoTicket.ABIERTO)));
        when(guiasClient.contarGuiasDeTicket("t-1")).thenReturn(2L);

        ApiException e = assertThrows(ApiException.class, () -> service.eliminar("t-1", "COORDINADOR"));

        assertEquals(HttpStatus.CONFLICT, e.getEstado());
        verify(ticketRepository, never()).delete(any());
    }

    @Test
    void eliminarSinGuiasBorraElTicket() {
        Ticket t = ticket(EstadoTicket.ABIERTO);
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(t));
        when(guiasClient.contarGuiasDeTicket("t-1")).thenReturn(0L);

        service.eliminar("t-1", "COORDINADOR");

        verify(ticketRepository).delete(t);
    }

    @Test
    void obtenerInexistenteLanza404() {
        when(ticketRepository.findById("x")).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.obtener("x"));

        assertEquals(HttpStatus.NOT_FOUND, e.getEstado());
    }

    @Test
    void listarConFiltroUsaLaConsultaPorEstado() {
        when(ticketRepository.findByEstadoOrderByNumeroDesc(EstadoTicket.ABIERTO))
                .thenReturn(List.of(ticket(EstadoTicket.ABIERTO)));

        assertEquals(1, service.listar(EstadoTicket.ABIERTO).size());
        verify(ticketRepository, never()).findAllByOrderByNumeroDesc();
    }

    @Test
    void historialDevuelveLosCambiosDelTicket() {
        Ticket t = ticket(EstadoTicket.TRANSITO);
        when(ticketRepository.findById("t-1")).thenReturn(Optional.of(t));
        when(cambioEstadoRepository.findByTicket_IdOrderByFechaDesc("t-1")).thenReturn(List.of(
                new CambioEstado(t, EstadoTicket.ABIERTO, EstadoTicket.TRANSITO, "En camino", List.of(), -33.4, -70.6)
        ));

        assertEquals(1, service.historial("t-1").size());
    }
}
