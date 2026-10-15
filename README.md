# App de Gestión de Tickets y Guías de Servicio

Aplicación Android nativa para una empresa de servicio técnico de climatización. El técnico en
terreno registra tickets de atención, avanza su estado hasta cerrarlos o cancelarlos, y emite una
guía de servicio por cada visita, con fotografías del trabajo y la ubicación donde se realizó.

Asignatura **DSY1105 — Desarrollo de Aplicaciones Móviles** · Evaluación Parcial 2

## Integrantes

| Nombre y apellido  | Responsabilidad |
|--------------------|---|
| _Mathias Soto_     | Cimiento del proyecto, listado y formulario de tickets |
| Camilo Godoy       | Detalle del ticket, estados, cierre, cancelación y guías de servicio |
| Patricio Ormazabal | Cámara, ubicación, permisos, animaciones y documentación |


## Funcionalidades

- Crear, listar, ver y editar tickets de atención, con número correlativo automático.
- Nueve estados de ticket, con Cerrado y Cancelado como estados finales de solo lectura.
- Registro del equipo de climatización atendido: nombre, modelo, número de serie y propiedad.
- Emisión de guías de servicio por visita, heredando cliente, dirección y equipo del ticket.
- Regla de negocio: un ticket no puede cerrarse sin al menos una guía de servicio emitida.
- Fotografías del trabajo tomadas con la cámara del dispositivo.
- Registro de las coordenadas del lugar de la visita mediante el GPS, con opción de abrirlas en la
  aplicación de mapas.
- Persistencia local completa: la información sobrevive al cierre de la aplicación.
- Funciona sin conexión a internet.

## Tecnología

| Elemento | Decisión |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose + Material 3 |
| Arquitectura | MVVM: UI → ViewModel → Repositorio → DAO → Room |
| Persistencia | Room sobre SQLite |
| Navegación | Navigation Compose |
| Recursos nativos | Cámara y ubicación (GPS) |
| SDK mínimo | API 26 |

## Estructura del proyecto

```
app/src/main/java/<paquete>/
├── data/
│   ├── local/          Entidades de Room, DAO, base de datos y conversores
│   └── repository/     Única puerta de entrada a los datos
├── domain/
│   ├── model/          Enum y modelos del negocio
│   └── validation/     Reglas de validación, independientes de la UI
├── ui/
│   ├── screens/        Una pantalla por archivo
│   ├── components/     Componentes compartidos (campo de formulario validado)
│   └── theme/          Colores, tipografía y color por estado
├── viewmodel/          Estado y lógica de cada pantalla
└── util/
```

Ninguna pantalla accede a Room ni a los DAO: todo pasa por la capa de repositorio. La lógica de
validación vive en los ViewModel y en `domain/validation`, nunca dentro de un *composable*.

## Cómo ejecutar

1. Clonar el repositorio.
2. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle.
3. Crear o seleccionar un dispositivo virtual con API 26 o superior, o conectar un teléfono con
   depuración USB activada.
4. Ejecutar la configuración `app`.

Para probar la cámara y la ubicación conviene un dispositivo físico. En el emulador, la cámara debe
estar habilitada en la configuración del dispositivo virtual y la ubicación se simula desde los
controles extendidos del emulador.

## Recursos nativos del dispositivo

| Recurso | Para qué | Permiso |
|---|---|---|
| Cámara | Fotografiar el trabajo realizado y adjuntarlo a la guía de servicio | `CAMERA` |
| Ubicación (GPS) | Registrar las coordenadas donde se realizó el servicio | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` |

Los permisos se solicitan en el momento en que la función se usa, no al abrir la aplicación. Si el
usuario los deniega, la aplicación explica para qué sirven, ofrece abrir los ajustes del sistema y
permite seguir trabajando: la guía se guarda igual, sin fotografías ni coordenadas.

Las fotografías se guardan como archivos en el almacenamiento privado de la aplicación y se
comparten con la cámara del sistema mediante `FileProvider`. En la base de datos solo queda su URI.

## Documentación

- `docs/plan-equipo.md` — reparto del trabajo, calendario y reglas de trabajo en equipo.
- `docs/cimiento.md` — decisiones de arquitectura y por qué está así.
- `docs/recursos-nativos.md` — cómo usar los componentes de cámara, ubicación y galería.
