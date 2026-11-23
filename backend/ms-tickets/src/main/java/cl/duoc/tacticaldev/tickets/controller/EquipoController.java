package cl.duoc.tacticaldev.tickets.controller;

import cl.duoc.tacticaldev.tickets.dto.EquipoResponse;
import cl.duoc.tacticaldev.tickets.service.EquipoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consulta de equipos. Se crean y editan junto con el ticket. */
@RestController
@RequestMapping("/api/v1/equipos")
public class EquipoController {

    private final EquipoService service;

    public EquipoController(EquipoService service) {
        this.service = service;
    }

    @GetMapping
    public List<EquipoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EquipoResponse obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @GetMapping("/serie/{serie}")
    public EquipoResponse buscarPorSerie(@PathVariable String serie) {
        return service.buscarPorSerie(serie);
    }
}
