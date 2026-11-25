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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired private MockMvc mvc;
    @MockBean private TicketService service;

    private TicketResponse respuesta() {
        return new TicketResponse("t-1", 1, "No enfria", "El equipo no enfria", "Banco Uno",
                "Av. Siempre Viva 123", Prioridad.ALTA, EstadoTicket.ABIERTO,
                new EquipoResponse("eq-1", "Split", "LG", "ABC123", Propiedad.ARRENDADO),
                LocalDateTime.now(), null, null, null, 7L, "Tecnico de Prueba");
    }

    @Test
    void listarDevuelveLosTickets() throws Exception {
        when(service.listar(null)).thenReturn(List.of(respuesta()));

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
}
