package cl.duoc.tacticaldev.tickets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    /** Cliente HTTP para conversar con el microservicio de guias. */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
