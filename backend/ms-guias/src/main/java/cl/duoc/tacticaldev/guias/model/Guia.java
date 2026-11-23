package cl.duoc.tacticaldev.guias.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tabla "guias". El ticket y el equipo viven en ms-tickets, por eso aqui solo se
 * guarda su id (en microservicios cada uno es dueno de su base de datos).
 */
@Entity
@Table(name = "guias")
public class Guia {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false, length = 36)
    private String ticketId;

    @Column(nullable = false, length = 36)
    private String equipoId;

    @Column(nullable = false, length = 250)
    private String direccion;

    private Double latitud;

    private Double longitud;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 100)
    private String tecnico;

    @Column(nullable = false, length = 2000)
    private String trabajoRealizado;

    @Column(length = 1000)
    private String repuestos;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false, length = 100)
    private String recibidoPor;

    /** Las URI de las fotos van en su propia tabla: guia_fotos. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guia_fotos", joinColumns = @JoinColumn(name = "guia_id"))
    @Column(name = "uri", length = 500)
    private List<String> urisDeFotos = new ArrayList<>();

    @PrePersist
    void antesDeGuardar() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getEquipoId() { return equipoId; }
    public void setEquipoId(String equipoId) { this.equipoId = equipoId; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTecnico() { return tecnico; }
    public void setTecnico(String tecnico) { this.tecnico = tecnico; }

    public String getTrabajoRealizado() { return trabajoRealizado; }
    public void setTrabajoRealizado(String trabajoRealizado) { this.trabajoRealizado = trabajoRealizado; }

    public String getRepuestos() { return repuestos; }
    public void setRepuestos(String repuestos) { this.repuestos = repuestos; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getRecibidoPor() { return recibidoPor; }
    public void setRecibidoPor(String recibidoPor) { this.recibidoPor = recibidoPor; }

    public List<String> getUrisDeFotos() { return urisDeFotos; }
    public void setUrisDeFotos(List<String> urisDeFotos) { this.urisDeFotos = urisDeFotos; }
}
