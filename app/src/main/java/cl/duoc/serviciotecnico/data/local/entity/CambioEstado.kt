package cl.duoc.serviciotecnico.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.serviciotecnico.domain.model.EstadoTicket
import java.time.LocalDateTime
import java.util.UUID

/** Registro del historial: de que estado a cual, con comentario, fotos y ubicacion. */
@Entity(
    tableName = "cambios_estado",
    foreignKeys = [
        ForeignKey(
            entity = Ticket::class,
            parentColumns = ["id"],
            childColumns = ["ticketId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ticketId"])]
)
data class CambioEstado(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val ticketId: String,
    val estadoAnterior: EstadoTicket,
    val estadoNuevo: EstadoTicket,
    val comentario: String,
    val urisDeFotos: List<String> = emptyList(),
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fecha: LocalDateTime = LocalDateTime.now()
)
