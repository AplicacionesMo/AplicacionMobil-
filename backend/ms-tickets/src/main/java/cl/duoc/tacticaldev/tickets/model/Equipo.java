package cl.duoc.tacticaldev.tickets.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

/** Tabla "equipos". El numero de serie es unico: un mismo equipo puede tener varios tickets. */
@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false, unique = true, length = 60)
    private String serie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Propiedad propiedad;

    public Equipo() {
    }

    public Equipo(String nombre, String modelo, String serie, Propiedad propiedad) {
        this.nombre = nombre;
        this.modelo = modelo;
        this.serie = serie;
        this.propiedad = propiedad;
    }

    /** El id es un UUID, igual que en la app, y lo asigna el servidor al guardar. */
    @PrePersist
    void asignarId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }
}
