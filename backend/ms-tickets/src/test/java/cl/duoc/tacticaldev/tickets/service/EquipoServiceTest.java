package cl.duoc.tacticaldev.tickets.service;

import cl.duoc.tacticaldev.tickets.dto.EquipoRequest;
import cl.duoc.tacticaldev.tickets.exception.ApiException;
import cl.duoc.tacticaldev.tickets.model.Equipo;
import cl.duoc.tacticaldev.tickets.model.Propiedad;
import cl.duoc.tacticaldev.tickets.repository.EquipoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock private EquipoRepository repository;
    @InjectMocks private EquipoService service;

    @Test
    void siLaSerieExisteReutilizaElEquipo() {
        Equipo existente = new Equipo("Viejo", "M1", "ABC123", Propiedad.ARRENDADO);
        existente.setId("eq-1");
        when(repository.findBySerieIgnoreCase("ABC123")).thenReturn(Optional.of(existente));
        when(repository.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipo r = service.obtenerOCrear(new EquipoRequest("Nuevo", "M2", "abc123", Propiedad.PROPIO_DEL_CLIENTE));

        assertSame(existente, r);
        assertEquals("Nuevo", r.getNombre());
        assertEquals(Propiedad.PROPIO_DEL_CLIENTE, r.getPropiedad());
    }

    @Test
    void siLaSerieNoExisteCreaUnoNuevoEnMayusculas() {
        when(repository.findBySerieIgnoreCase("XYZ9")).thenReturn(Optional.empty());
        when(repository.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipo r = service.obtenerOCrear(new EquipoRequest("Split", "LG", " xyz9 ", Propiedad.ARRENDADO));

        assertEquals("XYZ9", r.getSerie());
    }

    @Test
    void buscarSerieInexistenteLanza404() {
        when(repository.findBySerieIgnoreCase("NO")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.buscarPorSerie("NO"));
    }
}
