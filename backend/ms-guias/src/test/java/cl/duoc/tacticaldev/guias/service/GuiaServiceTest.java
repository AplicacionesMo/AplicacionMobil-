package cl.duoc.tacticaldev.guias.service;

import cl.duoc.tacticaldev.guias.dto.GuiaRequest;
import cl.duoc.tacticaldev.guias.dto.GuiaResponse;
import cl.duoc.tacticaldev.guias.exception.ApiException;
import cl.duoc.tacticaldev.guias.model.Guia;
import cl.duoc.tacticaldev.guias.repository.GuiaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuiaServiceTest {

    @Mock private GuiaRepository repository;
    @InjectMocks private GuiaService service;

    private GuiaRequest request(List<String> fotos) {
        return new GuiaRequest("t-1", "eq-1", " Av. Siempre Viva 123 ", "Juan Perez",
                "Cambio de capacitor y limpieza de filtros", null, "Sin observaciones",
                "Maria Soto", -33.45, -70.66, fotos);
    }

    @Test
    void crearAsignaNumeroCorrelativoYGuardaLasFotos() {
        when(repository.ultimoNumero()).thenReturn(4);
        when(repository.save(any(Guia.class))).thenAnswer(inv -> inv.getArgument(0));

        GuiaResponse r = service.crear(request(List.of("content://foto1", "content://foto2")));

        assertEquals(5, r.numero());
        assertEquals("Av. Siempre Viva 123", r.direccion());
        assertEquals(2, r.urisDeFotos().size());
        assertEquals("", r.repuestos());
    }

    @Test
    void crearSinFotosDejaListaVacia() {
        when(repository.ultimoNumero()).thenReturn(0);
        when(repository.save(any(Guia.class))).thenAnswer(inv -> inv.getArgument(0));

        GuiaResponse r = service.crear(request(null));

        assertTrue(r.urisDeFotos().isEmpty());
    }

    @Test
    void contarPorTicketDelegaEnElRepositorio() {
        when(repository.countByTicketId("t-1")).thenReturn(3L);

        assertEquals(3L, service.contarPorTicket("t-1"));
    }

    @Test
    void listarConTicketFiltra() {
        when(repository.findByTicketIdOrderByNumeroDesc("t-1")).thenReturn(List.of(new Guia()));

        assertEquals(1, service.listar("t-1").size());
        verify(repository, never()).findAllByOrderByNumeroDesc();
    }

    @Test
    void listarSinTicketDevuelveTodas() {
        when(repository.findAllByOrderByNumeroDesc()).thenReturn(List.of(new Guia(), new Guia()));

        assertEquals(2, service.listar(null).size());
    }

    @Test
    void actualizarCambiaLosDatos() {
        Guia existente = new Guia();
        existente.setId("g-1");
        when(repository.findById("g-1")).thenReturn(Optional.of(existente));
        when(repository.save(any(Guia.class))).thenAnswer(inv -> inv.getArgument(0));

        GuiaResponse r = service.actualizar("g-1", request(List.of()));

        assertEquals("Juan Perez", r.tecnico());
    }

    @Test
    void obtenerInexistenteLanza404() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.obtener("x"));

        assertEquals(HttpStatus.NOT_FOUND, e.getEstado());
    }

    @Test
    void eliminarBorraLaGuia() {
        Guia existente = new Guia();
        when(repository.findById("g-1")).thenReturn(Optional.of(existente));

        service.eliminar("g-1");

        verify(repository).delete(existente);
    }
}
