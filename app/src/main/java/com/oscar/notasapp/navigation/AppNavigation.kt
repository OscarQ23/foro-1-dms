package com.oscar.notasapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oscar.notasapp.ui.alumnos.AlumnoDetalleScreen
import com.oscar.notasapp.ui.alumnos.AlumnoFormScreen
import com.oscar.notasapp.ui.alumnos.AlumnosScreen
import com.oscar.notasapp.ui.asignaciones.AsignacionFormScreen
import com.oscar.notasapp.ui.asignaciones.AsignacionesScreen
import com.oscar.notasapp.ui.asignaciones.MateriaAlumnosScreen
import com.oscar.notasapp.ui.home.HomeScreen
import com.oscar.notasapp.ui.login.LoginScreen
import com.oscar.notasapp.ui.login.RegisterScreen
import com.oscar.notasapp.ui.materias.MateriaFormScreen
import com.oscar.notasapp.ui.materias.MateriasScreen
import com.oscar.notasapp.ui.resultado.ResultadoScreen

/**
 * Define las rutas y el grafo de navegación.
 *
 * Flujo principal:
 *   login → (register opcional) → home/{profesorId}
 *           ├── materias/{profesorId}    ↔ materias/{profesorId}/form/{materiaId}
 *           ├── alumnos/{profesorId}     ↔ alumnos/{profesorId}/form/{alumnoId}
 *           │                            ↘ alumnos/{profesorId}/detalle/{alumnoId}
 *           │                              → resultado/{promedio}/{aprobado}
 *           └── asignaciones/{profesorId} ↔ asignaciones/{profesorId}/form/{asignacionId}
 *
 * profesorId viaja en cada ruta para que las pantallas filtren los datos
 * por sesión sin depender de un estado global.
 */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home/{profesorId}"

    const val MATERIAS = "materias/{profesorId}"
    const val MATERIA_FORM = "materias/{profesorId}/form/{materiaId}"

    const val ALUMNOS = "alumnos/{profesorId}"
    const val ALUMNO_FORM = "alumnos/{profesorId}/form/{alumnoId}"
    const val ALUMNO_DETALLE = "alumnos/{profesorId}/detalle/{alumnoId}"

    const val ASIGNACIONES = "asignaciones/{profesorId}"
    const val MATERIA_ALUMNOS = "asignaciones/{profesorId}/materia/{materiaId}"
    const val EVALUAR = "asignaciones/{profesorId}/evaluar/{materiaId}/{alumnoId}"

    const val RESULTADO = "resultado/{promedio}/{aprobado}"

    fun home(profesorId: Long) = "home/$profesorId"

    fun materias(profesorId: Long) = "materias/$profesorId"
    fun materiaForm(profesorId: Long, materiaId: Long) =
        "materias/$profesorId/form/$materiaId"

    fun alumnos(profesorId: Long) = "alumnos/$profesorId"
    fun alumnoForm(profesorId: Long, alumnoId: Long) =
        "alumnos/$profesorId/form/$alumnoId"
    fun alumnoDetalle(profesorId: Long, alumnoId: Long) =
        "alumnos/$profesorId/detalle/$alumnoId"

    fun asignaciones(profesorId: Long) = "asignaciones/$profesorId"
    fun materiaAlumnos(profesorId: Long, materiaId: Long) =
        "asignaciones/$profesorId/materia/$materiaId"
    fun evaluar(profesorId: Long, materiaId: Long, alumnoId: Long) =
        "asignaciones/$profesorId/evaluar/$materiaId/$alumnoId"

    fun resultado(promedio: Double, aprobado: Boolean) =
        "resultado/$promedio/$aprobado"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { profesorId ->
                    navController.navigate(Routes.home(profesorId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { profesorId ->
                    navController.navigate(Routes.home(profesorId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.HOME,
            arguments = listOf(navArgument("profesorId") { type = NavType.LongType })
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            HomeScreen(
                profesorNombre = "Docente",
                onMaterias = { navController.navigate(Routes.materias(profesorId)) },
                onAlumnos = { navController.navigate(Routes.alumnos(profesorId)) },
                onAsignaciones = { navController.navigate(Routes.asignaciones(profesorId)) },
                onCerrarSesion = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- MATERIAS ----------------
        composable(
            route = Routes.MATERIAS,
            arguments = listOf(navArgument("profesorId") { type = NavType.LongType })
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            MateriasScreen(
                profesorId = profesorId,
                onAgregar = { navController.navigate(Routes.materiaForm(profesorId, 0L)) },
                onEditar = { id -> navController.navigate(Routes.materiaForm(profesorId, id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.MATERIA_FORM,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.LongType },
                navArgument("materiaId") { type = NavType.LongType }
            )
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            val materiaId = backStack.arguments?.getLong("materiaId") ?: 0L
            MateriaFormScreen(
                profesorId = profesorId,
                materiaId = if (materiaId == 0L) null else materiaId,
                onGuardado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------------- ALUMNOS ----------------
        composable(
            route = Routes.ALUMNOS,
            arguments = listOf(navArgument("profesorId") { type = NavType.LongType })
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            AlumnosScreen(
                profesorId = profesorId,
                onAgregar = { navController.navigate(Routes.alumnoForm(profesorId, 0L)) },
                onEditar = { id -> navController.navigate(Routes.alumnoForm(profesorId, id)) },
                onDetalle = { id -> navController.navigate(Routes.alumnoDetalle(profesorId, id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ALUMNO_FORM,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.LongType },
                navArgument("alumnoId") { type = NavType.LongType }
            )
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            val alumnoId = backStack.arguments?.getLong("alumnoId") ?: 0L
            AlumnoFormScreen(
                profesorId = profesorId,
                alumnoId = if (alumnoId == 0L) null else alumnoId,
                onGuardado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ALUMNO_DETALLE,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.LongType },
                navArgument("alumnoId") { type = NavType.LongType }
            )
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            val alumnoId = backStack.arguments?.getLong("alumnoId") ?: 0L
            AlumnoDetalleScreen(
                profesorId = profesorId,
                alumnoId = alumnoId,
                onCalcular = { promedio, aprobado ->
                    navController.navigate(Routes.resultado(promedio, aprobado))
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------------- ASIGNACIONES ----------------
        composable(
            route = Routes.ASIGNACIONES,
            arguments = listOf(navArgument("profesorId") { type = NavType.LongType })
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            AsignacionesScreen(
                profesorId = profesorId,
                onAbrirMateria = { materiaId ->
                    navController.navigate(Routes.materiaAlumnos(profesorId, materiaId))
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.MATERIA_ALUMNOS,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.LongType },
                navArgument("materiaId") { type = NavType.LongType }
            )
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            val materiaId = backStack.arguments?.getLong("materiaId") ?: 0L
            MateriaAlumnosScreen(
                profesorId = profesorId,
                materiaId = materiaId,
                onEvaluar = { alumnoId, mId ->
                    navController.navigate(Routes.evaluar(profesorId, mId, alumnoId))
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EVALUAR,
            arguments = listOf(
                navArgument("profesorId") { type = NavType.LongType },
                navArgument("materiaId") { type = NavType.LongType },
                navArgument("alumnoId") { type = NavType.LongType }
            )
        ) { backStack ->
            val profesorId = backStack.arguments?.getLong("profesorId") ?: 0L
            val materiaId = backStack.arguments?.getLong("materiaId") ?: 0L
            val alumnoId = backStack.arguments?.getLong("alumnoId") ?: 0L
            AsignacionFormScreen(
                profesorId = profesorId,
                materiaId = materiaId,
                alumnoId = alumnoId,
                onGuardado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------------- RESULTADO ----------------
        composable(
            route = Routes.RESULTADO,
            arguments = listOf(
                navArgument("promedio") { type = NavType.FloatType },
                navArgument("aprobado") { type = NavType.BoolType }
            )
        ) { backStack ->
            val promedio = backStack.arguments?.getFloat("promedio")?.toDouble() ?: 0.0
            val aprobado = backStack.arguments?.getBoolean("aprobado") ?: false
            ResultadoScreen(
                promedio = promedio,
                aprobado = aprobado,
                onVolver = { navController.popBackStack() },
                onCerrarSesion = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
