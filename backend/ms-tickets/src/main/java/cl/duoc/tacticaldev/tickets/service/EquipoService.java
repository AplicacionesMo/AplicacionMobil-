package cl.duoc.tacticaldev.tickets.service;

import cl.duoc.tacticaldev.tickets.dto.EquipoRequest;
import cl.duoc.tacticaldev.tickets.dto.EquipoResponse;
import cl.duoc.tacticaldev.tickets.exception.ApiException;
import cl.duoc.tacticaldev.tickets.model.Equipo;
import cl.duoc.tacticaldev.tickets.repository.EquipoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository repository;

    public EquipoService(EquipoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listar() {
        return repository.findAll().stream().map(EquipoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(String id) {
        return EquipoResponse.desde(repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipo no encontrado")));
    }

    @Transactional(readOnly = true)
    public EquipoResponse buscarPorSerie(String serie) {
        return EquipoResponse.desde(repository.findBySerieIgnoreCase(serie.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No hay un equipo con esa serie")));
    }

    /**
     * Si ya existe un equipo con esa serie lo actualiza y lo reutiliza; si no, lo crea.
     * Asi un mismo equipo acumula todos sus tickets.
     */
    @Transactional
    public Equipo obtenerOCrear(EquipoRequest request) {
        String serie = request.serie().trim().toUpperCase();
        Equipo equipo = repository.findBySerieIgnoreCase(serie)
                .orElseGet(() -> new Equipo(null, null, serie, null));
        equipo.setNombre(request.nombre().trim());
        equipo.setModelo(request.modelo().trim());
        equipo.setSerie(serie);
        equipo.setPropiedad(request.propiedad());
        return repository.save(equipo);
    }
}
