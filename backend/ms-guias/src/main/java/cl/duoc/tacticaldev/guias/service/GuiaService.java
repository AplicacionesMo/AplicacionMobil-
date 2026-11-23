package cl.duoc.tacticaldev.guias.service;

import cl.duoc.tacticaldev.guias.dto.GuiaRequest;
import cl.duoc.tacticaldev.guias.dto.GuiaResponse;
import cl.duoc.tacticaldev.guias.exception.ApiException;
import cl.duoc.tacticaldev.guias.model.Guia;
import cl.duoc.tacticaldev.guias.repository.GuiaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GuiaService {

    private final GuiaRepository repository;

    public GuiaService(GuiaRepository repository) {
        this.repository = repository;
    }

    /** Sin ticketId devuelve todas; con ticketId, solo las de ese ticket. */
    @Transactional(readOnly = true)
    public List<GuiaResponse> listar(String ticketId) {
        List<Guia> guias = (ticketId == null || ticketId.isBlank())
                ? repository.findAllByOrderByNumeroDesc()
                : repository.findByTicketIdOrderByNumeroDesc(ticketId);
        return guias.stream().map(GuiaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public GuiaResponse obtener(String id) {
        return GuiaResponse.desde(buscar(id));
    }

    /** Endpoint que consume ms-tickets antes de cerrar o eliminar un ticket. */
    @Transactional(readOnly = true)
    public long contarPorTicket(String ticketId) {
        return repository.countByTicketId(ticketId);
    }

    @Transactional
    public GuiaResponse crear(GuiaRequest request) {
        Guia guia = new Guia();
        guia.setNumero(repository.ultimoNumero() + 1);
        guia.setTicketId(request.ticketId());
        guia.setEquipoId(request.equipoId());
        copiarDatos(request, guia);
        return GuiaResponse.desde(repository.save(guia));
    }

    @Transactional
    public GuiaResponse actualizar(String id, GuiaRequest request) {
        Guia guia = buscar(id);
        copiarDatos(request, guia);
        return GuiaResponse.desde(repository.save(guia));
    }

    @Transactional
    public void eliminar(String id) {
        repository.delete(buscar(id));
    }

    private void copiarDatos(GuiaRequest request, Guia guia) {
        guia.setDireccion(request.direccion().trim());
        guia.setTecnico(request.tecnico().trim());
        guia.setTrabajoRealizado(request.trabajoRealizado().trim());
        guia.setRepuestos(textoOVacio(request.repuestos()));
        guia.setObservaciones(textoOVacio(request.observaciones()));
        guia.setRecibidoPor(request.recibidoPor().trim());
        guia.setLatitud(request.latitud());
        guia.setLongitud(request.longitud());
        guia.setUrisDeFotos(request.urisDeFotos() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.urisDeFotos()));
    }

    private String textoOVacio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private Guia buscar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Guia no encontrada"));
    }
}
