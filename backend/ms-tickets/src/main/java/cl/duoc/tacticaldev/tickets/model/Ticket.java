package cl.duoc.tacticaldev.tickets.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tabla "tickets". Cada ticket apunta a un equipo (muchos tickets -> un equipo)
 * y tiene su historial de cambios de estado (un ticket -> muchos cambios).
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 150)
    private String cliente;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridad prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaCierre;

    @Column(length = 1000)
    private String solucion;

    @Column(length = 1000)
    private String motivoCancelacion;

    /** Al eliminar el ticket se elimina su historial (cascade + orphanRemoval). */
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC")
    private List<CambioEstado> historial = new ArrayList<>();

    @PrePersist
    void antesDeGuardar() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }

    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public String getSolucion() { return solucion; }
    public void setSolucion(String solucion) { this.solucion = solucion; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public List<CambioEstado> getHistorial() { return historial; }
    public void setHistorial(List<CambioEstado> historial) { this.historial = historial; }
}
