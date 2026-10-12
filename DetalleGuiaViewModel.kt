
package cl.duoc.serviciotecnico.ui.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.serviciotecnico.data.repository.GuiaRepository

class DetalleGuiaViewModel(
    private val repository: GuiaRepository
) : ViewModel() {

    fun observarGuia(guiaId: String) =
        repository.observarGuia(guiaId)

    fun observarFotos(guiaId: String) =
        repository.observarFotos(guiaId)
}
