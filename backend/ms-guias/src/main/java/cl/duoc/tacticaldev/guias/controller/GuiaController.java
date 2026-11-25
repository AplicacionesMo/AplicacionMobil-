package cl.duoc.tacticaldev.guias.controller;

import cl.duoc.tacticaldev.guias.dto.GuiaRequest;
import cl.duoc.tacticaldev.guias.dto.GuiaResponse;
import cl.duoc.tacticaldev.guias.service.GuiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
@RequestMapping("/api/v1/guias")
public class GuiaController {

    private final GuiaService service;

    public GuiaController(GuiaService service) {
        this.service = service;
    }

    /** GET /api/v1/guias?ticketId=...  */
    @GetMapping
    public List<GuiaResponse> listar(@RequestParam(required = false) String ticketId) {
        return service.listar(ticketId);
    }

    @GetMapping("/{id}")
    public GuiaResponse obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    /** Lo consume ms-tickets (comunicacion entre microservicios). */
    @GetMapping("/ticket/{ticketId}/cantidad")
    public long contarPorTicket(@PathVariable String ticketId) {
        return service.contarPorTicket(ticketId);
    }

    /** La cabecera X-Rol identifica a quien pide: solo el tecnico emite guias. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuiaResponse crear(@Valid @RequestBody GuiaRequest request,
                              @RequestHeader(value = "X-Rol", required = false) String rol) {
        return service.crear(request, rol);
    }

    @PutMapping("/{id}")
    public GuiaResponse actualizar(@PathVariable String id, @Valid @RequestBody GuiaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
