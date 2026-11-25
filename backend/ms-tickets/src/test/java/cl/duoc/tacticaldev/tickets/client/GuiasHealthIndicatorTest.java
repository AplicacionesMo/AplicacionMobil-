package cl.duoc.tacticaldev.tickets.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * El indicador que sale en /actuator/health. Es el que delata cuando ms-tickets
 * no logra hablar con ms-guias, que fue exactamente la falla al finalizar tickets.
 */
@ExtendWith(MockitoExtension.class)
class GuiasHealthIndicatorTest {

    private static final String URL = "http://ms-guias:8083";

    @Mock
    private RestTemplate restTemplate;

    @Test
    void siMsGuiasRespondeQuedaArribaConLaUrl() {
        when(restTemplate.getForObject(eq(URL + "/actuator/health"), eq(String.class)))
                .thenReturn("{\"status\":\"UP\"}");
        GuiasHealthIndicator indicador = new GuiasHealthIndicator(restTemplate, URL);

        Health salud = indicador.health();

        assertEquals(Status.UP, salud.getStatus());
        assertEquals(URL, salud.getDetails().get("url"));
    }

    @Test
    void siMsGuiasNoRespondeQuedaAbajoYExplicaPorQue() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));
        GuiasHealthIndicator indicador = new GuiasHealthIndicator(restTemplate, URL);

        Health salud = indicador.health();

        assertEquals(Status.DOWN, salud.getStatus());
        assertEquals(URL, salud.getDetails().get("url"));
        assertTrue(salud.getDetails().get("error").toString().contains("No responde ms-guias"));
    }
}
