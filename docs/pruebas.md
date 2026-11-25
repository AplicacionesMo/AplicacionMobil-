# Pruebas unitarias

## Cómo ejecutarlas

| Dónde | Comando | Herramientas |
|---|---|---|
| App | `./gradlew testDebugUnitTest` o clic derecho en `app/src/test` → *Run Tests* | JUnit 4, MockK, kotlinx-coroutines-test |
| Cobertura de la lógica | `./gradlew coberturaLogica` → `app/build/reports/jacoco/coberturaLogica/html/index.html` | JaCoCo |
| Microservicios | `mvn test` dentro de cada carpeta de `backend/` | JUnit 5, Mockito, MockMvc |

La tarea `coberturaLogica` (definida al final de `app/build.gradle.kts`) mide solo la lógica:
ViewModel, repositorios, dominio, mapeos y manejo de errores. Deja fuera lo que no es lógica:
pantallas Compose, tema, navegación, clases generadas, DTO y entidades (solo guardan datos) y
las clases que dependen del sistema Android. El porcentaje total del informe es el que pide la
pauta (mínimo 80 %).

## Qué se prueba en la app

| Archivo | Qué valida | Qué se mockea |
|---|---|---|
| `ValidacionesTest` | Reglas de los formularios (vacío, largo mínimo, correo, fecha) | Nada: Kotlin puro |
| `ReglasDeDominioTest` | Estados finales, permisos por rol, traducción del clima y recomendación | Nada |
| `MapeosTest` | Conversión JSON (DTO) → entidad de Room, valores por defecto seguros | Nada |
| `ErrorDeRedTest` | Mensaje del servidor, mensaje por código HTTP, sin conexión | Nada (se arma un `HttpException` real) |
| `TicketRepositoryTest` | POST/PUT/PATCH/DELETE llaman a la API y guardan en caché; 404 borra local; rol en header | `TicketsApi` y los DAO |
| `GuiaRepositoryTest` | Crear guía guarda sus fotos; sincronizar elimina las que ya no existen | `GuiasApi` y los DAO |
| `SesionRepositoryTest` | Login guarda el rol; login fallido no guarda; rol por defecto | `UsuariosApi` y `AlmacenSesion` |
| `ClimaYEquipoRepositoryTest` | La API externa se convierte a dominio | `ClimaApi` |
| `LoginViewModelTest` | Formulario inválido no llama al servidor; 401 y sin red muestran mensaje | `SesionRepository` |
| `ListadoTicketsViewModelTest` | Sincroniza al abrir, aviso sin red, filtro por estado, clima | Repositorios de tickets, sesión y clima |
| `FormularioTicketViewModelTest` | Crear y editar, serie en mayúsculas, ticket cerrado de solo lectura | Repositorios de tickets y equipos |
| `DetalleTicketViewModelTest` | Validaciones del cambio de estado, no cerrar sin guías, permisos de rol, eliminar | Repositorios de tickets, guías, equipos y sesión |
| `FormularioGuiaViewModelTest` / `DetalleGuiaViewModelTest` | Guía válida se envía con equipo, fotos y GPS; errores de red | Repositorios de tickets, guías y equipos |

### Cómo se mockea (MockK)

```kotlin
private val api: TicketsApi = mockk()                 // mock estricto: todo debe configurarse
private val ticketDao: TicketDao = mockk(relaxed = true) // relajado: lo no configurado no hace nada

coEvery { api.crear(any()) } returns Datos.ticketDto() // "cuando llamen a crear, responde esto"
coEvery { api.obtener("t-1") } throws Datos.errorHttp(404) // simular un error del servidor

coVerify { ticketDao.upsert(match { it.id == "t-1" }) } // "verifica que se guardó en Room"
coVerify(exactly = 0) { api.crear(any()) }              // "verifica que NO se llamó"
```

- `coEvery` / `coVerify` son para funciones `suspend` (las de Retrofit y Room).
- `slot()` + `capture()` guardan el argumento enviado para revisarlo después.
- `MainDispatcherRule` reemplaza `Dispatchers.Main` (que no existe en la JVM) por un dispatcher de
  prueba, para que `viewModelScope` funcione.
- Los `StateFlow` creados con `stateIn(WhileSubscribed)` solo se actualizan si alguien los escucha:
  en la prueba se lanza un colector en `backgroundScope`, igual que lo hace la pantalla.

## Qué se prueba en los microservicios

| Archivo | Qué valida |
|---|---|
| `UsuarioServiceTest` | Login correcto/incorrecto/inactivo, clave guardada con BCrypt, correo duplicado (409) |
| `AuthControllerTest` | `POST /auth/login` responde 200, 401 con mensaje y 400 si falta el correo |
| `TicketServiceTest` | Número correlativo, no editar cerrado, historial, no cerrar sin guías, solo coordinador cancela y elimina |
| `EquipoServiceTest` | Reutilizar equipo por serie, serie en mayúsculas |
| `TicketControllerTest` | Rutas, 201 al crear, 400 con mensaje de validación, 403/204 al eliminar |
| `GuiaServiceTest` / `GuiaControllerTest` | Correlativo, fotos, conteo por ticket, validación del trabajo realizado |

En el backend se mockea el repositorio JPA y el `GuiasClient` (la llamada a ms-guias) con Mockito,
y en los `@WebMvcTest` el servicio con `@MockBean`. Así las pruebas no necesitan MySQL ni los otros
microservicios encendidos.

## Por qué aportan seguridad y estabilidad

- **Detectan regresiones**: si alguien cambia una regla (por ejemplo, permite cerrar sin guía), la
  prueba falla antes de llegar al usuario.
- **Prueban los casos difíciles de provocar a mano**: servidor caído, 401, 403, 409, 404.
- **Documentan las reglas**: cada nombre de prueba describe una regla del negocio.
- **Son rápidas y aisladas**: corren en segundos, sin emulador ni base de datos, por eso se pueden
  ejecutar en cada cambio.
- **Seguridad**: comprueban que el técnico no puede cancelar ni eliminar y que la clave nunca se
  guarda en texto plano.
