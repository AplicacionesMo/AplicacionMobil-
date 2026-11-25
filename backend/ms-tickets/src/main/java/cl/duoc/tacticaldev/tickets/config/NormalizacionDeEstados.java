package cl.duoc.tacticaldev.tickets.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Los estados pasaron de nueve a cuatro (Abierto, Transito, Finalizado, Cancelado).
 * Las filas guardadas con los estados antiguos ya no corresponden a ninguna constante
 * del enum, y al leerlas Hibernate falla. Esto las traduce una sola vez al arrancar.
 */
@Component
public class NormalizacionDeEstados {

    private static final Logger log = LoggerFactory.getLogger(NormalizacionDeEstados.class);

    /** Estado antiguo -> estado nuevo equivalente. */
    private static final Map<String, String> EQUIVALENCIAS = Map.of(
            "COORDINACION_EN_PROCESO", "TRANSITO",
            "COORDINADO", "TRANSITO",
            "TRABAJO_EN_PROCESO", "TRANSITO",
            "PENDIENTE_POR_CLIENTE", "TRANSITO",
            "PENDIENTE_POR_TACTICALDEV", "TRANSITO",
            "CERRADO", "FINALIZADO"
    );

    /** Tabla y columna de cada lugar donde se guarda un estado. */
    private static final List<String[]> COLUMNAS = List.of(
            new String[]{"tickets", "estado"},
            new String[]{"cambios_estado", "estado_anterior"},
            new String[]{"cambios_estado", "estado_nuevo"}
    );

    private final JdbcTemplate jdbcTemplate;

    public NormalizacionDeEstados(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void normalizar() {
        int total = 0;
        for (String[] columna : COLUMNAS) {
            for (Map.Entry<String, String> equivalencia : EQUIVALENCIAS.entrySet()) {
                total += actualizar(columna[0], columna[1], equivalencia.getKey(), equivalencia.getValue());
            }
        }
        if (total > 0) {
            log.info("Estados antiguos traducidos a los nuevos: {} filas", total);
        }
    }

    private int actualizar(String tabla, String columna, String antiguo, String nuevo) {
        String sql = "UPDATE " + tabla + " SET " + columna + " = ? WHERE " + columna + " = ?";
        try {
            return jdbcTemplate.update(sql, nuevo, antiguo);
        } catch (Exception e) {
            // La tabla todavia no existe (primer arranque): no hay nada que traducir
            log.debug("No se pudo normalizar {}.{}: {}", tabla, columna, e.getMessage());
            return 0;
        }
    }
}
