package cl.duoc.serviciotecnico.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.serviciotecnico.ui.screens.DetalleGuiaScreen
import cl.duoc.serviciotecnico.ui.screens.DetalleTicketScreen
import cl.duoc.serviciotecnico.ui.screens.FormularioGuiaScreen
import cl.duoc.serviciotecnico.ui.screens.FormularioTicketScreen
import cl.duoc.serviciotecnico.ui.screens.ListadoTicketsScreen
import cl.duoc.serviciotecnico.ui.screens.PruebaRecursosScreen

private const val DURACION = 320

@Composable
fun NavegacionApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LISTADO_TICKETS,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION)
            ) + fadeIn(animationSpec = tween(DURACION))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION)
            ) + fadeOut(animationSpec = tween(DURACION))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION)
            ) + fadeIn(animationSpec = tween(DURACION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION)
            ) + fadeOut(animationSpec = tween(DURACION))
        }
    ) {

        composable(Rutas.LISTADO_TICKETS) {
            ListadoTicketsScreen(
                onCrearTicket = { navController.navigate(Rutas.formularioTicket()) },
                onAbrirTicket = { ticketId -> navController.navigate(Rutas.detalleTicket(ticketId)) },
                onProbarRecursos = { navController.navigate(Rutas.PRUEBA_RECURSOS) }
            )
        }

        composable(
            route = Rutas.FORMULARIO_TICKET,
            arguments = listOf(
                navArgument(Rutas.ARG_TICKET_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entrada ->
            val ticketId = entrada.arguments?.getString(Rutas.ARG_TICKET_ID).orEmpty()
            FormularioTicketScreen(
                ticketId = ticketId.ifBlank { null },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.DETALLE_TICKET,
            arguments = listOf(navArgument(Rutas.ARG_TICKET_ID) { type = NavType.StringType })
        ) { entrada ->
            val ticketId = entrada.arguments?.getString(Rutas.ARG_TICKET_ID).orEmpty()
            DetalleTicketScreen(
                ticketId = ticketId,
                onVolver = { navController.popBackStack() },
                onEditarTicket = { navController.navigate(Rutas.formularioTicket(ticketId)) },
                onNuevaGuia = { navController.navigate(Rutas.formularioGuia(ticketId)) },
                onAbrirGuia = { guiaId -> navController.navigate(Rutas.detalleGuia(guiaId)) }
            )
        }

        composable(
            route = Rutas.FORMULARIO_GUIA,
            arguments = listOf(navArgument(Rutas.ARG_TICKET_ID) { type = NavType.StringType })
        ) { entrada ->
            val ticketId = entrada.arguments?.getString(Rutas.ARG_TICKET_ID).orEmpty()
            FormularioGuiaScreen(
                ticketId = ticketId,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.DETALLE_GUIA,
            arguments = listOf(navArgument(Rutas.ARG_GUIA_ID) { type = NavType.StringType })
        ) { entrada ->
            val guiaId = entrada.arguments?.getString(Rutas.ARG_GUIA_ID).orEmpty()
            DetalleGuiaScreen(
                guiaId = guiaId,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PRUEBA_RECURSOS) {
            PruebaRecursosScreen(onVolver = { navController.popBackStack() })
        }
    }
}
