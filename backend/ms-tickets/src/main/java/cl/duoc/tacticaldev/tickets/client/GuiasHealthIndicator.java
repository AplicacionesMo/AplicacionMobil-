package cl.duoc.tacticaldev.tickets.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Aparece en GET /actuator/health como "guias".
 * Permite ver de inmediato si ms-tickets logra comunicarse con ms-guias.
 */
@Component("guias")
public class GuiasHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String urlBase;

    public GuiasHealthIndicator(RestTemplate restTemplate, @Value("${servicios.guias.url}") String urlBase) {
        this.restTemplate = restTemplate;
        this.urlBase = urlBase;
    }

    @Override
    public Health health() {
        try {
            restTemplate.getForObject(urlBase + "/actuator/health", String.class);
            return Health.up().withDetail("url", urlBase).build();
        } catch (RestClientException e) {
            return Health.down()
                    .withDetail("url", urlBase)
                    .withDetail("error", "No responde ms-guias: " + e.getMessage())
                    .build();
        }
    }
}
