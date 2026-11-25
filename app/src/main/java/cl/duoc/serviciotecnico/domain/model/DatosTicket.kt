package cl.duoc.serviciotecnico.domain.model

/** Lo que el tecnico escribe en el formulario de ticket + equipo (formularios 1 y 2). */
data class DatosTicket(
    val titulo: String,
    val descripcion: String,
    val cliente: String,
    val direccion: String,
    val prioridad: Prioridad,
    val nombreEquipo: String,
    val modelo: String,
    val serie: String,
    val propiedad: Propiedad,
    val tecnicoId: Long? = null,
    val tecnicoNombre: String? = null
)
