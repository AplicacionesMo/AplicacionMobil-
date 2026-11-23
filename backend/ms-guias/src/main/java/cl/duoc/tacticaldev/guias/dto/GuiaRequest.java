package cl.duoc.tacticaldev.guias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GuiaRequest(
        @NotBlank(message = "El ticket es obligatorio")
        String ticketId,

        @NotBlank(message = "El equipo es obligatorio")
        String equipoId,

        @NotBlank(message = "La direccion es obligatoria")
        String direccion,

        @NotBlank(message = "El nombre del tecnico es obligatorio")
        String tecnico,

        @NotBlank(message = "El trabajo realizado es obligatorio")
        @Size(min = 10, message = "El trabajo realizado debe tener al menos 10 caracteres")
        String trabajoRealizado,

        String repuestos,

        String observaciones,

        @NotBlank(message = "Quien recibe el servicio es obligatorio")
        String recibidoPor,

        Double latitud,

        Double longitud,

        @Size(max = 15, message = "Maximo 15 fotos por guia")
        List<String> urisDeFotos
) {
}
