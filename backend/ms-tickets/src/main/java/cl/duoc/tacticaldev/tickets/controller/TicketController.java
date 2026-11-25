package cl.duoc.tacticaldev.tickets.controller;

import cl.duoc.tacticaldev.tickets.dto.CambioEstadoRequest;
import cl.duoc.tacticaldev.tickets.dto.CambioEstadoResponse;
import cl.duoc.tacticaldev.tickets.dto.TicketRequest;
import cl.duoc.tacticaldev.tickets.dto.TicketResponse;
import cl.duoc.tacticaldev.tickets.model.EstadoTicket;
import cl.duoc.tacticaldev.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de tickets. El header X-Rol indica el perfil de quien hace la peticion
 * (TECNICO, COORDINADOR o ADMIN) y se usa para las acciones restringidas.
 */
@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping
    public List<TicketResponse> listar(@RequestParam(required = false) EstadoTicket estado) {
        return service.listar(estado);
    }

    @GetMapping("/{id}")
    public TicketResponse obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse crear(@Valid @RequestBody TicketRequest request,
                                @RequestHeader(value = "X-Rol", defaultValue = "TECNICO") String rol) {
        return service.crear(request, rol);
    }

    @PutMapping("/{id}")
    public TicketResponse actualizar(@PathVariable String id, @Valid @RequestBody TicketRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public TicketResponse cambiarEstado(@PathVariable String id,
                                        @Valid @RequestBody CambioEstadoRequest request,
                                        @RequestHeader(value = "X-Rol", defaultValue = "TECNICO") String rol) {
        return service.cambiarEstado(id, request, rol);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id,
                         @RequestHeader(value = "X-Rol", defaultValue = "TECNICO") String rol) {
        service.eliminar(id, rol);
    }

    @GetMapping("/{id}/historial")
    public List<CambioEstadoResponse> historial(@PathVariable String id) {
        return service.historial(id);
    }
}
