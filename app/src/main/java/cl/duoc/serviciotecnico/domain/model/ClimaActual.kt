package cl.duoc.serviciotecnico.domain.model

/**
 * Clima actual entregado por la API externa. Para una empresa de climatizacion
 * la temperatura exterior anticipa la demanda: con calor fallan los aires acondicionados.
 */
data class ClimaActual(
    val temperatura: Double,
    val humedad: Int,
    val viento: Double,
    val codigo: Int
) {
    val descripcion: String get() = describir(codigo)
    val recomendacion: String get() = recomendar(temperatura)

    /** Linea principal de la tarjeta: "Santiago ahora: 21,5 °C · Despejado". */
    val titulo: String get() = "Santiago ahora: ${"%.1f".format(temperatura)} °C · $descripcion"

    /**
     * Linea secundaria: "Humedad 45% · Viento 12 km/h".
     * El numero se formatea por separado y recien despues se arma el texto: si el % de la
     * humedad entra a String.format, el formateador lo lee como un especificador y lanza
     * UnknownFormatConversionException.
     */
    val resumen: String get() = "Humedad $humedad% · Viento ${"%.0f".format(viento)} km/h"

    companion object {
        const val UMBRAL_CALOR = 28.0
        const val UMBRAL_FRIO = 10.0

        /** Codigos WMO que usa Open-Meteo. */
        fun describir(codigo: Int): String = when (codigo) {
            0 -> "Despejado"
            1, 2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Neblina"
            in 51..57 -> "Llovizna"
            in 61..67, in 80..82 -> "Lluvia"
            in 71..77, 85, 86 -> "Nieve"
            in 95..99 -> "Tormenta"
            else -> "Sin datos"
        }

        fun recomendar(temperatura: Double): String = when {
            temperatura >= UMBRAL_CALOR -> "Calor: se espera alta demanda de aire acondicionado"
            temperatura <= UMBRAL_FRIO -> "Frío: priorizar calefacción y bombas de calor"
            else -> "Templado: buen momento para mantenciones preventivas"
        }
    }
}
