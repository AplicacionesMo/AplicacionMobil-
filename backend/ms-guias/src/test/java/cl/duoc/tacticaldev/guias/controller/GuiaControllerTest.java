package cl.duoc.tacticaldev.guias.controller;

import cl.duoc.tacticaldev.guias.service.GuiaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
