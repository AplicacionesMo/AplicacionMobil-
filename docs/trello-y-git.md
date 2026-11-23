# Trello y GitHub

## Tablero Trello

Columnas: **Por hacer · En curso · En revisión · Listo**. Cada tarjeta lleva el nombre del
integrante, una etiqueta (Backend, App, Pruebas, Publicación) y una lista de verificación.

### Mathias Soto — Tickets

- [Backend] Proyecto `ms-tickets` con MySQL `tactical_tickets`
- [Backend] Entidades Ticket, Equipo y CambioEstado (relaciones y cascada)
- [Backend] Endpoints GET/POST/PUT/DELETE de tickets y GET de equipos
- [App] `TicketsApi` + DTO + mapeos
- [App] `TicketRepository` con caché Room (`@Upsert`, sincronizar)
- [App] Listado y formulario de ticket conectados al microservicio
- [Pruebas] `TicketServiceTest`, `TicketControllerTest`, `TicketRepositoryTest`, `FormularioTicketViewModelTest`, `ListadoTicketsViewModelTest`

### Camilo Godoy — Guías y estados

- [Backend] Proyecto `ms-guias` con MySQL `tactical_guias`
- [Backend] Endpoints de guías y `/ticket/{id}/cantidad`
- [Backend] `GuiasClient` en ms-tickets + regla "no cerrar sin guía" y "no eliminar con guías"
- [Backend] `PATCH /tickets/{id}/estado` con historial y permisos por `X-Rol`
- [App] `GuiasApi` + `GuiaRepository`
- [App] Detalle del ticket: cambio de estado, eliminar (coordinador), sincronizar
- [App] Pantalla de detalle de guía
- [Pruebas] `GuiaServiceTest`, `GuiaControllerTest`, `GuiaRepositoryTest`, `DetalleTicketViewModelTest`, `FormularioGuiaViewModelTest`

### Patricio Ormazabal — Usuarios, API externa y publicación

- [Backend] Proyecto `ms-usuarios` con BCrypt, roles y datos iniciales
- [Backend] `POST /auth/login` y CRUD de usuarios
- [App] `RetrofitService` (OkHttp, logging, timeouts) y `network_security_config`
- [App] Login con el microservicio, `AlmacenSesion`, roles en la app
- [App] API externa Open-Meteo: `ClimaApi`, `ClimaRepository`, tarjeta de clima
- [Publicación] Keystore `.jks`, `signingConfigs`, APK firmado
- [Docs] README, capturas, Swagger
- [Pruebas] `UsuarioServiceTest`, `AuthControllerTest`, `LoginViewModelTest`, `SesionRepositoryTest`, `MapeosTest`, `ErrorDeRedTest`

## Ramas y commits

Una rama por tarea, *pull request* revisado por otro integrante y cada uno sube **sus propios
commits** (el historial es la evidencia de participación individual).

| Rama | Dueño | Commit sugerido |
|---|---|---|
| `backend/ms-tickets` | Mathias | `feat(ms-tickets): entidades, repositorios y CRUD de tickets` |
| `app/tickets-remoto` | Mathias | `feat(app): TicketRepository con Retrofit y caché Room` |
| `test/tickets` | Mathias | `test: pruebas de tickets en app y backend` |
| `backend/ms-guias` | Camilo | `feat(ms-guias): CRUD de guías y conteo por ticket` |
| `backend/estados` | Camilo | `feat(ms-tickets): cambio de estado, historial y GuiasClient` |
| `app/guias-estados` | Camilo | `feat(app): detalle de ticket y guía conectados` |
| `test/guias-estados` | Camilo | `test: pruebas de guías y cambio de estado` |
| `backend/ms-usuarios` | Patricio | `feat(ms-usuarios): login con BCrypt y roles` |
| `app/login-clima` | Patricio | `feat(app): RetrofitService, login remoto y API de clima` |
| `release/apk-firmado` | Patricio | `build: firma release y APK v2.0` |
| `docs/readme` | Patricio | `docs: README con endpoints y pasos de ejecución` |

En la defensa: mostrar en GitHub *Insights → Contributors* y *Network*, y en Trello las tarjetas
propias movidas a "Listo".
