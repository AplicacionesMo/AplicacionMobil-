package cl.duoc.tacticaldev.tickets.client;

import cl.duoc.tacticaldev.tickets.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** La llamada a ms-guias se simula con un RestTemplate mock: no hace falta tener ms-guias encendido. */
class GuiasClientTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final GuiasClient client = new GuiasClient(restTemplate, "http://127.0.0.1:8083");

    @Test
    void devuelveLaCantidadQueRespondeMsGuias() {
        when(restTemplate.getForObject(anyString(), eq(Long.class), eq("t-1"))).thenReturn(2L);

        assertEquals(2L, client.contarGuiasDeTicket("t-1"));
    }

    @Test
    void siMsGuiasEstaApagadoResponde503ConMensajeClaro() {
        when(restTemplate.getForObject(anyString(), eq(Long.class), eq("t-1")))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ApiException e = assertThrows(ApiException.class, () -> client.contarGuiasDeTicket("t-1"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getEstado());
        assertTrue(e.getMessage().contains("No se pudo conectar"));
    }

    @Test
    void siMsGuiasRespondeErrorTambienResponde503() {
        when(restTemplate.getForObject(anyString(), eq(Long.class), eq("t-1")))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ApiException e = assertThrows(ApiException.class, () -> client.contarGuiasDeTicket("t-1"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getEstado());
        assertTrue(e.getMessage().contains("500"));
    }
}
