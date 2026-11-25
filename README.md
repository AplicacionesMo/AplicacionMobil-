# TacticalDEV · Servicio Técnico en Terreno

Aplicación Android nativa para una empresa de servicio técnico de climatización. El técnico en
terreno registra tickets de atención, avanza su estado hasta cerrarlos y emite una guía de servicio
por cada visita, con fotografías y la ubicación GPS. El coordinador crea, cancela y elimina
tickets, y el administrador además gestiona los usuarios.

La app se conecta a **tres microservicios propios en Spring Boot** (usuarios, tickets y guías, cada
uno con su propia base MySQL) y consume una **API externa** (Open-Meteo) para mostrar el clima
actual y anticipar la demanda de climatización.

Asignatura **DSY1105 — Desarrollo de Aplicaciones Móviles** · Evaluaciones Parciales 4 y 5

## 1. Integrantes

| Integrante | Responsabilidad |
|---|---|
| Mathias Soto | **Tickets**: microservicio `ms-tickets` (tickets, equipos, historial), integración Retrofit del listado y formulario, pruebas de tickets |
| Camilo Godoy | **Guías y estados**: microservicio `ms-guias`, comunicación `ms-tickets → ms-guias`, cambio de estado y detalle de guía, pruebas de detalle y guías |
| Patricio Ormazabal | **Usuarios, API externa y publicación**: microservicio `ms-usuarios` (login y roles), API de clima, `RetrofitService`, APK firmado, README y Trello |

## 2. Funcionalidades

- **Gestión de usuarios desde la app** (solo el administrador): listar, crear y eliminar.
  No se puede eliminar al último administrador.
- **Login contra el microservicio `ms-usuarios`**, con contraseña cifrada con BCrypt y dos perfiles:
  técnico, coordinador y administrador. La sesión queda guardada en el teléfono.
- **CRUD completo de tickets** contra `ms-tickets`: crear (POST), listar y ver (GET), editar (PUT),
  cambiar estado (PATCH) y eliminar (DELETE). Crear y eliminar tickets es solo para coordinador
  y administrador: **el técnico no crea tickets**, trabaja en los que le asignan.
- Registro del **equipo de climatización** (nombre, modelo, serie, propio o arrendado). Si la serie ya
  existe, el servidor reutiliza el equipo.
- **Cuatro estados** de ticket: Abierto, En tránsito, Finalizado y Cancelado. El ticket nace Abierto
  y cada cambio queda en el **historial** del servidor.
- **Guías de servicio** contra `ms-guias`, con fotografías de la cámara y coordenadas. La guía es el
  único lugar donde se documenta la visita: el cambio de estado no vuelve a pedir foto, ubicación ni
  comentario.
- **Reglas de negocio validadas en la app y otra vez en el servidor**:
  - no se puede finalizar un ticket sin al menos una guía (ms-tickets le pregunta a ms-guias);
    Abierto, En tránsito y Cancelado se alcanzan sin guía;
  - el técnico no puede crear, cancelar ni eliminar tickets (coordinador y administrador sí);
  - solo el administrador crea, edita o elimina usuarios;
  - un ticket con guías no se puede eliminar;
  - un ticket finalizado o cancelado es de solo lectura.
- **Clima actual en Santiago** (API externa Open-Meteo) con una recomendación para la operación.
- **Caché local con Room**: si el servidor no responde, la app sigue mostrando los últimos datos y avisa.
- Cámara, GPS, permisos en tiempo de uso y animaciones (de la evaluación anterior).

## 3. Arquitectura

```
App Android (Kotlin + Jetpack Compose, MVVM)

  Pantalla (Compose) ──eventos──▶ ViewModel ──▶ Repositorio ──┬──▶ Retrofit ──HTTP/JSON──▶ Microservicio
        ▲                            │                         │                               │
        └──────── StateFlow ◀────────┘                         └──▶ Room (caché) ◀── guarda la respuesta
                                                                        │
                                                       la pantalla observa Room con Flow

Microservicios (Spring Boot 3, Java 17)

  ms-usuarios :8081 ── MySQL tactical_usuarios
  ms-tickets  :8082 ── MySQL tactical_tickets ──RestTemplate──▶ ms-guias (¿cuántas guías tiene?)
  ms-guias    :8083 ── MySQL tactical_guias

  Cada uno: controller → service → repository (JPA) → entidad → tabla
```

**Por qué MVVM**: la pantalla solo dibuja el estado y avisa eventos; el ViewModel tiene la lógica y
las validaciones y sobrevive a la rotación; el repositorio decide de dónde vienen los datos. Así cada
capa se prueba por separado.

**Por qué microservicios separados**: cada uno es dueño de su base de datos y se puede desplegar o
caer sin botar a los otros. Si `ms-guias` está apagado, igual se pueden crear y editar tickets.

### Paquetes de la app

```
app/src/main/java/cl/duoc/serviciotecnico/
├── data/
│   ├── local/          Room: entidades, DAO y base (caché)
│   ├── remote/         RetrofitService, interfaces de API, DTO, mapeos y manejo de errores
│   └── repository/     Única puerta de entrada a los datos (API + caché)
├── domain/
│   ├── model/          Enum y modelos del negocio (estado, rol, clima...)
│   └── validation/     Reglas de validación en Kotlin puro
├── ui/
│   ├── screens/        Una pantalla por archivo
│   ├── components/     Componentes compartidos (cámara, ubicación, galería, campos)
│   ├── navigation/     Rutas y grafo de navegación
│   ├── viewmodel/      Estado y lógica de cada pantalla
│   └── theme/
└── util/
```

### Paquetes de cada microservicio

```
backend/ms-xxx/src/main/java/cl/duoc/tacticaldev/xxx/
├── controller/   Endpoints REST (@RestController)
├── service/      Reglas de negocio (@Service, @Transactional)
├── repository/   Acceso a datos (JpaRepository)
├── model/        Entidades JPA (@Entity) y enum
├── dto/          Lo que entra (Request, con @Valid) y lo que sale (Response)
├── exception/    ApiException + manejador global que responde {"estado", "mensaje"}
├── client/       (solo ms-tickets) cliente HTTP hacia ms-guias
└── config/       Beans y datos iniciales
```

## 4. Endpoints

### Microservicios propios

| Microservicio | Método | Endpoint | Para qué |
|---|---|---|---|
| ms-usuarios :8081 | POST | `/api/v1/auth/login` | Iniciar sesión → usuario y rol, o 401 |
| | GET | `/api/v1/usuarios` · `/api/v1/usuarios/{id}` | Listar / ver usuarios |
| | POST | `/api/v1/usuarios` · header `X-Rol` | Crear usuario (solo ADMIN; clave con BCrypt) |
| | PUT | `/api/v1/usuarios/{id}` · header `X-Rol` | Editar usuario (solo ADMIN) |
| | DELETE | `/api/v1/usuarios/{id}` · header `X-Rol` | Eliminar usuario (solo ADMIN, nunca el último admin) |
| ms-tickets :8082 | GET | `/api/v1/tickets` (opcional `?estado=ABIERTO`) | Listar tickets |
| | GET | `/api/v1/tickets/{id}` | Ver un ticket con su equipo |
| | POST | `/api/v1/tickets` · header `X-Rol` | Crear ticket + equipo (coordinador o admin; número correlativo automático) |
| | PUT | `/api/v1/tickets/{id}` | Editar (409 si está cerrado o cancelado) |
| | PATCH | `/api/v1/tickets/{id}/estado` · header `X-Rol` | Cambiar estado con comentario, fotos y GPS |
| | DELETE | `/api/v1/tickets/{id}` · header `X-Rol` | Eliminar (coordinador o admin, y sin guías) |
| | GET | `/api/v1/tickets/{id}/historial` | Historial de cambios de estado |
| | GET | `/api/v1/equipos` · `/{id}` · `/serie/{serie}` | Consultar equipos |
| ms-guias :8083 | GET | `/api/v1/guias?ticketId=...` | Guías de un ticket |
| | GET | `/api/v1/guias/{id}` | Ver una guía con sus fotos |
| | POST | `/api/v1/guias` | Emitir guía (número correlativo automático) |
| | PUT | `/api/v1/guias/{id}` | Editar guía |
| | DELETE | `/api/v1/guias/{id}` | Eliminar guía |
| | GET | `/api/v1/guias/ticket/{ticketId}/cantidad` | Cuántas guías tiene un ticket (lo usa ms-tickets) |

Con los microservicios encendidos, cada uno publica su documentación interactiva en
`http://localhost:808X/swagger-ui.html`.

### API externa

| API | Endpoint | Uso en la app |
|---|---|---|
| [Open-Meteo](https://open-meteo.com) (gratuita, sin llave) | `GET https://api.open-meteo.com/v1/forecast?latitude=-33.4489&longitude=-70.6693&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto` | Tarjeta "Santiago ahora" en el listado: temperatura, humedad, viento y recomendación (calor → demanda de aire acondicionado) |

**Diferencia entre la API externa y los microservicios**: los microservicios son nuestros, guardan
los datos del negocio, los escribimos y validamos nosotros, y la app los persiste en caché. La API
externa es de un tercero, solo se lee, no se guarda en Room ni se mezcla con los tickets, y si falla
la app sigue funcionando.

## 5. Pasos para ejecutar

### Requisitos

- Android Studio (Ladybug o superior) con un emulador API 26+.
- JDK 17 o superior.
- MySQL 8 (XAMPP, Laragon, MySQL Workbench o `docker compose up -d` en `backend/`), usuario `root`
  sin contraseña. Si no hay MySQL, cada microservicio trae un perfil `h2` de respaldo.
- IntelliJ IDEA o VS Code con extensión de Spring Boot (o Maven instalado).

### 1) Levantar los microservicios

Las bases de datos se crean solas la primera vez. Abrir cada carpeta de `backend/` como proyecto
Maven y ejecutar la clase `Ms...Application`, o desde una terminal:

```bash
cd backend/ms-usuarios && mvn spring-boot:run     # puerto 8081
cd backend/ms-guias    && mvn spring-boot:run     # puerto 8083
cd backend/ms-tickets  && mvn spring-boot:run     # puerto 8082
```

Sin MySQL: `mvn spring-boot:run -Dspring-boot.run.profiles=h2`.

Si MySQL tiene contraseña: definir `DB_PASS` (y `DB_USER` si no es root) antes de ejecutar.

Comprobar: abrir `http://localhost:8081/swagger-ui.html` (y 8082, 8083).

### 2) Ejecutar la app

1. Abrir la carpeta raíz del proyecto en Android Studio y esperar la sincronización de Gradle.
2. Ejecutar la configuración `app` en el emulador.
3. Ingresar con un usuario de prueba:

| Perfil | Correo | Clave |
|---|---|---|
| Técnico | `tecnico@tacticaldev.cl` | `tecnico123` |
| Coordinador | `coordinador@tacticaldev.cl` | `coord123` |
| Administrador | `admin@tacticaldev.cl` | `admin123` |

Los tres se crean solos al iniciar `ms-usuarios` (si ya existían, no se duplican).

El emulador llega al PC por `10.0.2.2`. En un teléfono físico, cambiar las URL en
`app/build.gradle.kts` (`URL_USUARIOS`, `URL_TICKETS`, `URL_GUIAS`) por la IP del PC y agregar esa IP
en `app/src/main/res/xml/network_security_config.xml`.

### Si un microservicio no conecta

Cada microservicio tiene un chequeo de salud. Abrir en el navegador del PC:

| URL | Qué debería decir |
|---|---|
| `http://localhost:8081/actuator/health` | `"status":"UP"` y `db` UP |
| `http://localhost:8082/actuator/health` | `"status":"UP"`, `db` UP y **`guias` UP** (conexión con ms-guias) |
| `http://localhost:8083/actuator/health` | `"status":"UP"` y `db` UP |

- **No abre la página:** ese microservicio no está corriendo o se cayó al iniciar. Revisar la
  consola de IntelliJ (errores típicos: MySQL apagado o el puerto ya ocupado por otro programa).
- **`db` DOWN:** MySQL está apagado o la clave no es la configurada (`DB_PASS`).
- **`guias` DOWN en ms-tickets:** ms-guias no está encendido. Sin él no se puede cerrar ni eliminar tickets.
- **Todo UP pero la app dice "sin conexión":** la app no llega al PC. En el emulador de Android
  Studio se usa `10.0.2.2`; **otros emuladores (Nox, MuMu, BlueStacks) o un teléfono real usan otra
  dirección**: hay que poner la IP del PC en `build.gradle.kts` y en `network_security_config.xml`.
- En Logcat, filtrando por `okhttp`, se ve cada petición con su URL y su respuesta.

### 3) Ejecutar las pruebas

```bash
# App (JUnit 4 + MockK + coroutines-test)
./gradlew testDebugUnitTest
./gradlew coberturaLogica   # cobertura de la lógica: app/build/reports/jacoco/coberturaLogica/html/index.html

# Microservicios (JUnit 5 + Mockito + MockMvc)
cd backend/ms-tickets && mvn test
```

Detalle de qué se prueba y qué se mockea: [`docs/pruebas.md`](docs/pruebas.md).

## 6. APK firmado

- APK: `apk/ServicioTecnico-v2.0-release.apk`
- Llave: `app/keystore/tacticaldev-release.jks` (alias `tacticaldev`)
- Configuración: `signingConfigs` en `app/build.gradle.kts`, datos en `keystore.properties`

Paso a paso de la generación y verificación: [`docs/firma-apk.md`](docs/firma-apk.md).

![APK firmado](docs/capturas/apk-firmado.png)
![Llave .jks](docs/capturas/jks.png)

> La llave y sus claves están en el repositorio solo porque la evaluación lo pide. En un proyecto
> real nunca se versionan.

## 7. Planificación y trabajo en equipo

- Tablero Trello: _agregar enlace_ — tarjetas por integrante, ver [`docs/trello-y-git.md`](docs/trello-y-git.md).
- Una rama por tarea y pull request revisado por otro integrante.

![Trello](docs/capturas/trello.png)

## Documentación adicional

- `docs/firma-apk.md` — generación y verificación del APK firmado.
- `docs/pruebas.md` — pruebas unitarias de la app y de los microservicios.
- `docs/trello-y-git.md` — tarjetas de Trello y ramas por integrante.
- `docs/cimiento.md` · `docs/recursos-nativos.md` — decisiones de la evaluación anterior.
