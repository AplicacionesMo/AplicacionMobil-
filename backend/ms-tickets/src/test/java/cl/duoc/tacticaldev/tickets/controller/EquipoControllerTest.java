package cl.duoc.tacticaldev.tickets.controller;

import cl.duoc.tacticaldev.tickets.dto.EquipoResponse;
import cl.duoc.tacticaldev.tickets.exception.ApiException;
import cl.duoc.tacticaldev.tickets.model.Propiedad;
import cl.duoc.tacticaldev.tickets.service.EquipoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Consulta de equipos: listar, ver uno y buscar por numero de serie. */
@WebMvcTest(EquipoController.class)
class EquipoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EquipoService service;

    private EquipoResponse equipo() {
        return new EquipoResponse("eq-1", "Split muro", "LG 12000", "ABC123", Propiedad.ARRENDADO);
    }

    @Test
    void listarDevuelveLosEquipos() throws Exception {
        when(service.listar()).thenReturn(List.of(equipo()));

        mvc.perform(get("/api/v1/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serie").value("ABC123"));
    }

    @Test
    void obtenerDevuelveElEquipo() throws Exception {
        when(service.obtener("eq-1")).thenReturn(equipo());

        mvc.perform(get("/api/v1/equipos/eq-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("LG 12000"));
    }

    /** La serie es la llave real del equipo: con ella se evita duplicarlo al crear un ticket. */
    @Test
    void buscarPorSerieDevuelveElEquipo() throws Exception {
        when(service.buscarPorSerie("ABC123")).thenReturn(equipo());

        mvc.perform(get("/api/v1/equipos/serie/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("eq-1"));
    }

    @Test
    void serieDesconocidaResponde404() throws Exception {
        when(service.buscarPorSerie("NOEXISTE"))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Equipo no encontrado"));

        mvc.perform(get("/api/v1/equipos/serie/NOEXISTE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Equipo no encontrado"));
    }
}
