# El cimiento, explicado

Qué hay en el proyecto antes de que empiece el reparto, y **por qué** está así. Sirve de guía para
trabajar sobre él y de preparación para la defensa: las preguntas de abajo son las que se hacen.

## Las capas

```
Pantalla (Compose)      ui/screens, ui/components
   |  observa estado, envia eventos
ViewModel               viewmodel/          <- las validaciones viven aqui
   |
Repositorio             data/repository/    <- unica puerta a los datos
   |
DAO                     data/local/dao/
   |
Room -> SQLite          data/local/
```

La regla, en una frase: **cada capa solo conoce la de abajo**. Una pantalla no puede llamar a un DAO
ni a Room; pide las cosas a su ViewModel, que las pide al repositorio.

## Por qué cada decisión

**¿Por qué las consultas de lectura devuelven `Flow` y no una lista?**
Una lista es una foto: si alguien inserta un ticket, la pantalla no se entera. Un `Flow` es un grifo:
Room vuelve a emitir la lista cada vez que la tabla cambia, y la pantalla se redibuja sola. Eso es lo
que la rúbrica llama "la interfaz responde a cambios de estado".

**¿Para qué sirve el repositorio, si casi solo llama al DAO?**
Para que el día que cambie la base de datos, el cambio se quede en una sola carpeta. Hoy parece una
capa de más; es seguro de mantenibilidad. También es lo que permite probar un ViewModel sin base de
datos, reemplazando el repositorio por uno falso.

**¿Por qué las validaciones no están en la pantalla?**
Tres razones. Compose vuelve a ejecutar la función de la pantalla en cada redibujo, así que lo que
viva ahí se recalcula y se pierde al rotar el teléfono. La misma regla hace falta en tres lugares
(deshabilitar el botón, pintar el error, verificar antes de guardar) y en el ViewModel se escribe una
sola vez. Y una regla en Kotlin puro se puede probar sin emulador.

**¿Por qué los estados son un `enum` y no texto?**
Porque con texto libre nada impide guardar `"cerrrado"`. Con un `enum`, el compilador lo impide y la
lista de estados existe en un solo archivo.

**¿Por qué el color del estado no está dentro del `enum`?**
Porque el `enum` es dominio y un color es interfaz. Si el dominio importara `Color`, no se podría
probar ni reutilizar fuera de Android. La correspondencia está en `ui/theme/EstadoColores.kt`.

**¿Por qué las fotos no se guardan en la base de datos?**
Porque una base de datos no es un sistema de archivos. La imagen va al almacenamiento privado de la
app y en la tabla queda su URI, que es texto corto.

**¿Qué pasa si dos pantallas abren la base de datos a la vez?**
No ocurre: `AppDatabase.obtener()` devuelve siempre la misma instancia, protegida con `synchronized`.
Dos conexiones a la misma base producen bloqueos y datos desincronizados.

## Qué hay construido

| Carpeta | Contenido |
|---|---|
| `domain/model` | Los tres enum: estado, prioridad, propiedad |
| `domain/validation` | Reglas de validación reutilizables, en Kotlin puro |
| `data/local/entity` | Las cuatro entidades: ticket, equipo, guía, foto |
| `data/local/dao` | Las consultas, incluida la que cuenta guías por ticket |
| `data/local` | Base de datos y conversores de fecha y enum |
| `data/repository` | Los tres repositorios |
| `ui/theme` | Colores, tipografía y color por estado |
| `ui/components` | Campo de texto validado, etiqueta de estado, andamio provisional |
| `ui/navigation` | Rutas y grafo de navegación |
| `ui/screens` | Las cinco pantallas, todavía provisionales |

## Cómo obtener un repositorio desde un ViewModel

Los repositorios están armados una sola vez en `ServicioTecnicoApp`. Desde un ViewModel se accede a
ellos con una *factory*, usando `viewModelFactory { initializer { ... } }` y la clave
`APPLICATION_KEY` para obtener la clase `Application`. En el archivo `ServicioTecnicoApp.kt` está el
ejemplo en el comentario.

## Reglas al trabajar sobre el cimiento

1. **Nadie edita el grafo de navegación.** Cada uno reemplaza el contenido de su propia pantalla.
2. **Nadie edita `build.gradle.kts`.** Todas las dependencias del proyecto ya están declaradas,
   incluidas las de cámara, ubicación e imágenes.
3. **Ningún `if` de validación dentro de un `@Composable`.** Si hace falta una regla nueva, se
   agrega a `domain/validation` y el ViewModel la usa.
4. **Todos los campos de formulario usan `CampoTextoValidado`.** Si le falta algo, se le pide el
   cambio al dueño del archivo; no se hace una copia propia.
5. **El andamio `PantallaProvisional` se borra** cuando ya no quede ninguna pantalla usándolo.
