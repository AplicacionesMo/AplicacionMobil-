package cl.duoc.tacticaldev.guias.controller;

import cl.duoc.tacticaldev.guias.dto.GuiaResponse;
import cl.duoc.tacticaldev.guias.exception.ApiException;
import cl.duoc.tacticaldev.guias.service.GuiaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuiaController.class)
class GuiaControllerTest {

    @Autowired private MockMvc mvc;
    @MockBean private GuiaService service;

    @Test
    void cantidadDevuelveUnNumero() throws Exception {
        when(service.contarPorTicket("t-1")).thenReturn(2L);

        mvc.perform(get("/api/v1/guias/ticket/t-1/cantidad"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void crearSinTrabajoRealizadoResponde400() throws Exception {
        mvc.perform(post("/api/v1/guias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId":"t-1","equipoId":"eq-1","direccion":"Av. 123",
                                 "tecnico":"Juan","trabajoRealizado":"corto","recibidoPor":"Maria"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El trabajo realizado debe tener al menos 10 caracteres"));
    }

    private GuiaResponse guia() {
        return new GuiaResponse("g-1", 1, "t-1", "eq-1", "Av. Siempre Viva 123",
                -33.45, -70.66, LocalDateTime.now(), "Tecnico de Prueba",
                "Cambio de capacitor y limpieza", "Capacitor 35uF", "", "Maria Soto",
                List.of("content://foto1"));
    }

    private static final String CUERPO = """
            {"ticketId":"t-1","equipoId":"eq-1","direccion":"Av. Siempre Viva 123",
             "tecnico":"Tecnico de Prueba","trabajoRealizado":"Cambio de capacitor y limpieza",
             "recibidoPor":"Maria Soto"}
            """;

    @Test
    void listarDevuelveLasGuias() throws Exception {
        when(service.listar(null)).thenReturn(List.of(guia()));

        mvc.perform(get("/api/v1/guias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value(1));
    }

    @Test
    void listarPorTicketFiltra() throws Exception {
        when(service.listar("t-1")).thenReturn(List.of(guia()));

        mvc.perform(get("/api/v1/guias").param("ticketId", "t-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketId").value("t-1"));
    }

    @Test
    void obtenerDevuelveLaGuia() throws Exception {
        when(service.obtener("g-1")).thenReturn(guia());

        mvc.perform(get("/api/v1/guias/g-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnico").value("Tecnico de Prueba"));
    }

    @Test
    void obtenerInexistenteResponde404() throws Exception {
        when(service.obtener("x")).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Guia no encontrada"));

        mvc.perform(get("/api/v1/guias/x"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Guia no encontrada"));
    }

    /** La guia la emite el tecnico: el rol viaja en X-Rol y el servidor lo revisa. */
    @Test
    void crearComoTecnicoResponde201() throws Exception {
        when(service.crear(any(), eq("TECNICO"))).thenReturn(guia());

        mvc.perform(post("/api/v1/guias")
                        .header("X-Rol", "TECNICO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("g-1"));
    }

    @Test
    void crearComoCoordinadorResponde403() throws Exception {
        when(service.crear(any(), eq("COORDINADOR")))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "Solo el tecnico puede emitir guias de servicio"));

        mvc.perform(post("/api/v1/guias")
                        .header("X-Rol", "COORDINADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el tecnico puede emitir guias de servicio"));
    }

    @Test
    void actualizarDevuelveLaGuiaEditada() throws Exception {
        when(service.actualizar(eq("g-1"), any())).thenReturn(guia());

        mvc.perform(put("/api/v1/guias/g-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recibidoPor").value("Maria Soto"));
    }

    @Test
    void eliminarResponde204() throws Exception {
        mvc.perform(delete("/api/v1/guias/g-1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar("g-1");
    }

    @Test
    void eliminarInexistenteResponde404() throws Exception {
        doThrow(new ApiException(HttpStatus.NOT_FOUND, "Guia no encontrada"))
                .when(service).eliminar("x");

        mvc.perform(delete("/api/v1/guias/x"))
                .andExpect(status().isNotFound());
    }
}
