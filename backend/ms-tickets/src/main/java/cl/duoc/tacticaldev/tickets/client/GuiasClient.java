package cl.duoc.tacticaldev.tickets.client;

import cl.duoc.tacticaldev.tickets.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Comunicacion entre microservicios: ms-tickets le pregunta a ms-guias
 * cuantas guias tiene un ticket, antes de cerrarlo o eliminarlo.
 */
@Component
public class GuiasClient {

    private static final Logger log = LoggerFactory.getLogger(GuiasClient.class);

    private final RestTemplate restTemplate;
    private final String urlBase;

    public GuiasClient(RestTemplate restTemplate, @Value("${servicios.guias.url}") String urlBase) {
        this.restTemplate = restTemplate;
        this.urlBase = urlBase;
    }

    public long contarGuiasDeTicket(String ticketId) {
        String url = urlBase + "/api/v1/guias/ticket/{ticketId}/cantidad";
        try {
            Long cantidad = restTemplate.getForObject(url, Long.class, ticketId);
            return cantidad == null ? 0 : cantidad;
        } catch (ResourceAccessException e) {
            // No hubo respuesta: ms-guias apagado, puerto equivocado o tiempo agotado
            log.error("No se pudo conectar con ms-guias en {}: {}", urlBase, e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo conectar con el microservicio de guias (" + urlBase + "). Verifica que este encendido.");
        } catch (HttpStatusCodeException e) {
            // ms-guias respondio, pero con un error
            log.error("ms-guias respondio {} al contar guias del ticket {}", e.getStatusCode(), ticketId);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El microservicio de guias respondio con error " + e.getStatusCode().value() + ". Intentalo nuevamente.");
        } catch (RestClientException e) {
            log.error("Error inesperado al consultar ms-guias: {}", e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El microservicio de guias no esta disponible. Intentalo nuevamente.");
        }
    }
}
