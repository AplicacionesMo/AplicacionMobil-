package cl.duoc.tacticaldev.tickets.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestConfig {

    /**
     * Cliente HTTP para conversar con el microservicio de guias.
     * Con tiempos maximos: si ms-guias no responde, se corta a los pocos segundos
     * y la app recibe un 503 claro, en vez de quedar esperando.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}
