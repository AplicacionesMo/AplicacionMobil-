package cl.duoc.tacticaldev.tickets.controller;

import cl.duoc.tacticaldev.tickets.dto.EquipoResponse;
import cl.duoc.tacticaldev.tickets.dto.TicketResponse;
import cl.duoc.tacticaldev.tickets.exception.ApiException;
import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import cl.duoc.tacticaldev.tickets.model.Prioridad;
import cl.duoc.tacticaldev.tickets.model.Propiedad;
import cl.duoc.tacticaldev.tickets.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired private MockMvc mvc;
    @MockBean private TicketService service;

    private static final String CUERPO_TICKET = """
            {"titulo":"No enfria","descripcion":"El equipo no enfria la sala",
             "cliente":"Banco Uno","direccion":"Av. Siempre Viva 123","prioridad":"ALTA",
             "equipo":{"nombre":"Split","modelo":"LG","serie":"ABC123","propiedad":"ARRENDADO"}}
            """;

    private TicketResponse respuesta() {
        return new TicketResponse("t-1", 1, "No enfria", "El equipo no enfria", "Banco Uno",
                "Av. Siempre Viva 123", Prioridad.ALTA, EstadoTicket.ABIERTO,
                new EquipoResponse("eq-1", "Split", "LG", "ABC123", Propiedad.ARRENDADO),
                LocalDateTime.now(), null, null, null, 7L, "Tecnico de Prueba");
    }

    @Test
    void listarDevuelveLosTickets() throws Exception {
        when(service.listar(null, "TECNICO", null)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(1))
                .andExpect(jsonPath("$[0].equipo.serie").value("ABC123"));
    }

    @Test
    void crearResponde201() throws Exception {
        when(service.crear(any(), eq("COORDINADOR"))).thenReturn(respuesta());

        mvc.perform(post("/api/v1/tickets")
                        .header("X-Rol", "COORDINADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"No enfria","descripcion":"El equipo no enfria",
                                 "cliente":"Banco Uno","direccion":"Av. Siempre Viva 123","prioridad":"ALTA",
                                 "equipo":{"nombre":"Split","modelo":"LG","serie":"ABC123","propiedad":"ARRENDADO"}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("t-1"));
    }

    @Test
    void crearConTituloCortoResponde400ConMensaje() throws Exception {
        mvc.perform(post("/api/v1/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"No","descripcion":"El equipo no enfria",
                                 "cliente":"Banco Uno","direccion":"Av. Siempre Viva 123","prioridad":"ALTA",
                                 "equipo":{"nombre":"Split","modelo":"LG","serie":"ABC123","propiedad":"ARRENDADO"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El titulo debe tener al menos 5 caracteres"));
    }

    @Test
    void crearComoTecnicoResponde403() throws Exception {
        when(service.crear(any(), eq("TECNICO")))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "Solo el coordinador o el administrador pueden crear tickets"));

        mvc.perform(post("/api/v1/tickets")
                        .header("X-Rol", "TECNICO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"No enfria","descripcion":"El equipo no enfria",
                                 "cliente":"Banco Uno","direccion":"Av. Siempre Viva 123","prioridad":"ALTA",
                                 "equipo":{"nombre":"Split","modelo":"LG","serie":"ABC123","propiedad":"ARRENDADO"}}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el coordinador o el administrador pueden crear tickets"));
    }

    @Test
    void eliminarComoTecnicoResponde403() throws Exception {
        doThrow(new ApiException(HttpStatus.FORBIDDEN, "Solo el coordinador puede eliminar un ticket"))
                .when(service).eliminar(eq("t-1"), eq("TECNICO"));

        mvc.perform(delete("/api/v1/tickets/t-1").header("X-Rol", "TECNICO"))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarComoCoordinadorResponde204() throws Exception {
        mvc.perform(delete("/api/v1/tickets/t-1").header("X-Rol", "COORDINADOR"))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerDevuelveElTicket() throws Exception {
        when(service.obtener("t-1", "TECNICO", null)).thenReturn(respuesta());

        mvc.perform(get("/api/v1/tickets/t-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("No enfria"))
                .andExpect(jsonPath("$.tecnicoNombre").value("Tecnico de Prueba"));
    }

    /** El tecnico que no tiene el ticket asignado recibe 403, no el contenido. */
    @Test
    void obtenerTicketAjenoResponde403() throws Exception {
        when(service.obtener("t-1", "TECNICO", 99L))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "Este ticket esta asignado a otro tecnico"));

        mvc.perform(get("/api/v1/tickets/t-1")
                        .header("X-Rol", "TECNICO")
                        .header("X-Usuario-Id", "99"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Este ticket esta asignado a otro tecnico"));
    }

    @Test
    void listarComoTecnicoPasaSuIdAlServicio() throws Exception {
        when(service.listar(null, "TECNICO", 7L)).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/v1/tickets")
                        .header("X-Rol", "TECNICO")
                        .header("X-Usuario-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(1));

        verify(service).listar(null, "TECNICO", 7L);
    }

    @Test
    void actualizarDevuelveElTicketEditado() throws Exception {
        when(service.actualizar(eq("t-1"), any(), eq("COORDINADOR"), any())).thenReturn(respuesta());

        mvc.perform(put("/api/v1/tickets/t-1")
                        .header("X-Rol", "COORDINADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_TICKET))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Banco Uno"));
    }

    @Test
    void cambiarEstadoDevuelveElTicketActualizado() throws Exception {
        when(service.cambiarEstado(eq("t-1"), any(), eq("TECNICO"), any())).thenReturn(respuesta());

        mvc.perform(patch("/api/v1/tickets/t-1/estado")
                        .header("X-Rol", "TECNICO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estadoNuevo\":\"TRANSITO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void historialDevuelveLosCambios() throws Exception {
        when(service.historial("t-1")).thenReturn(List.of());

        mvc.perform(get("/api/v1/tickets/t-1/historial"))
                .andExpect(status().isOk());
    }
}
