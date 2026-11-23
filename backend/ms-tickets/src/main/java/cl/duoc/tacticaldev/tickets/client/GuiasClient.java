package cl.duoc.tacticaldev.tickets.client;

import cl.duoc.tacticaldev.tickets.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Comunicacion entre microservicios: ms-tickets le pregunta a ms-guias
 * cuantas guias tiene un ticket, antes de cerrarlo o eliminarlo.
 */
@Component
public class GuiasClient {

    private final RestTemplate restTemplate;
    private final String urlBase;

    public GuiasClient(RestTemplate restTemplate, @Value("${servicios.guias.url}") String urlBase) {
        this.restTemplate = restTemplate;
        this.urlBase = urlBase;
    }

    public long contarGuiasDeTicket(String ticketId) {
        try {
            Long cantidad = restTemplate.getForObject(
                    urlBase + "/api/v1/guias/ticket/{ticketId}/cantidad", Long.class, ticketId);
            return cantidad == null ? 0 : cantidad;
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El microservicio de guias no esta disponible. Intentalo nuevamente.");
        }
    }
}
