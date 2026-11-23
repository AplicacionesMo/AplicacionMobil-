package cl.duoc.tacticaldev.tickets.dto;

import cl.duoc.tacticaldev.tickets.model.Equipo;
import cl.duoc.tacticaldev.tickets.model.Propiedad;

public record EquipoResponse(String id, String nombre, String modelo, String serie, Propiedad propiedad) {

    public static EquipoResponse desde(Equipo e) {
        return new EquipoResponse(e.getId(), e.getNombre(), e.getModelo(), e.getSerie(), e.getPropiedad());
    }
}
