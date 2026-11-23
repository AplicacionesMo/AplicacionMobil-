package cl.duoc.tacticaldev.tickets.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tabla "cambios_estado": quien cambio que, cuando, donde (GPS) y con que fotos. */
@Entity
@Table(name = "cambios_estado")
public class CambioEstado {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoTicket estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoTicket estadoNuevo;

    @Column(nullable = false, length = 1000)
    private String comentario;

    /**
     * Las URI de las fotos van en su propia tabla: cambio_estado_fotos.
     * Carga perezosa (LAZY): se leen dentro de la transaccion del servicio.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "cambio_estado_fotos", joinColumns = @JoinColumn(name = "cambio_id"))
    @Column(name = "uri", length = 500)
    private List<String> urisDeFotos = new ArrayList<>();

    private Double latitud;

    private Double longitud;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public CambioEstado() {
    }

    public CambioEstado(Ticket ticket, EstadoTicket estadoAnterior, EstadoTicket estadoNuevo,
                        String comentario, List<String> urisDeFotos, Double latitud, Double longitud) {
        this.ticket = ticket;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.comentario = comentario;
        this.urisDeFotos = urisDeFotos == null ? new ArrayList<>() : new ArrayList<>(urisDeFotos);
        this.latitud = latitud;
        this.longitud = longitud;
        this.fecha = LocalDateTime.now();
    }

    @PrePersist
    void asignarId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public EstadoTicket getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(EstadoTicket estadoAnterior) { this.estadoAnterior = estadoAnterior; }

    public EstadoTicket getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(EstadoTicket estadoNuevo) { this.estadoNuevo = estadoNuevo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public List<String> getUrisDeFotos() { return urisDeFotos; }
    public void setUrisDeFotos(List<String> urisDeFotos) { this.urisDeFotos = urisDeFotos; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
