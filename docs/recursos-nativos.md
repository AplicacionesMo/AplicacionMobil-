# Recursos nativos: cómo usarlos

Los dos recursos del dispositivo están construidos como componentes independientes. No son
pantallas: se insertan en cualquier formulario y devuelven un valor hacia afuera. Quien los use no
necesita saber nada de permisos ni de archivos.

## Botón de fotografía

`BotonDeFoto` en `ui/components`.

| Recibe | Qué es |
|---|---|
| `onFotoTomada: (Uri) -> Unit` | Se llama una vez por cada foto tomada, con la URI del archivo ya guardado |
| `etiqueta` | Texto del botón; por defecto "Agregar fotografia" |

Qué hace por dentro: pide el permiso de cámara si falta, crea el archivo en el almacenamiento
privado (`filesDir/fotos`), abre la cámara del sistema y entrega la URI a través de `FileProvider`.
Si el usuario cancela la toma, borra el archivo vacío. Si deniega el permiso, muestra el diálogo
explicativo y no pasa nada más: el formulario sigue usable.

Lo que debe hacer quien lo use: acumular las URI en una lista y pasarlas a
`GuiaRepository.crear(urisDeFotos = ...)` al guardar.

## Botón de ubicación

`BotonDeUbicacion` en `ui/components`.

| Recibe | Qué es |
|---|---|
| `onUbicacionObtenida: (Double, Double) -> Unit` | Latitud y longitud, en ese orden |
| `etiqueta` | Texto del botón; por defecto "Usar mi ubicacion" |

Pide el permiso, muestra un indicador de progreso mientras consulta el GPS, y si no logra una
posición nueva recurre a la última conocida. Si tampoco hay, muestra el mensaje de error él mismo.

Lo que debe hacer quien lo use: guardar los dos números en `latitud` y `longitud` de la guía. Si no
llegan, se guardan nulos y la guía se crea igual.

## Galería de fotos

`GaleriaDeFotos` recibe `fotos: List<Uri>` y, opcionalmente, `onQuitarFoto: (Uri) -> Unit`. Si se
pasa la segunda, cada miniatura muestra el botón de quitar. Sin ella, la galería es de solo lectura
— que es como va en el detalle de la guía.

Al quitar una foto hay que llamar también a `ArchivosDeFoto.eliminar(context, uri)` para borrar el
archivo del disco, no solo de la lista.

## Abrir el mapa

`Mapas.abrirCoordenadas(context, latitud, longitud, etiqueta)` abre la aplicación de mapas del
dispositivo en ese punto. `Mapas.abrirDireccion(context, direccion)` hace lo mismo con una dirección
escrita, para cuando la guía no tiene coordenadas.

Esto no cuenta como recurso nativo: delega en otra aplicación. Los recursos son la cámara y el GPS.

## Componentes de apoyo

- `BotonGuardar`: botón que muestra un indicador de progreso mientras se escribe en la base de
  datos. Recibe `habilitado` y `guardando`, los dos calculados por el ViewModel.
- `DialogoPermisoDenegado`: diálogo con la explicación y acceso a los ajustes del sistema. Ya lo
  usan los dos botones; no hace falta invocarlo a mano.

## Cómo probarlo

La pantalla `PruebaRecursosScreen` tiene los dos botones, la galería, la etiqueta de estado animada
y un campo validado. Se llega a ella desde el listado de tickets, con el botón "Probar camara y
ubicacion".

Esa pantalla es temporal: cuando los componentes ya estén insertados en el formulario de guía, se
borran el archivo, su ruta en `Rutas` y su entrada en `NavegacionApp`.

**En emulador:** la cámara debe estar habilitada en la configuración del dispositivo virtual. La
ubicación se simula desde los controles extendidos (los tres puntos al costado del emulador →
Location → fijar un punto → Set Location). Sin fijar un punto, el GPS del emulador devuelve nulo y
verás el mensaje de error, que es el comportamiento correcto.

**En teléfono físico:** funciona directo, pero hay que aceptar los permisos cuando los pida.

## Animaciones ya hechas

| Requisito | Dónde |
|---|---|
| RF-28, transición entre pantallas | `NavegacionApp`: deslizamiento más fundido, ida y vuelta |
| RF-29, cambio de estado animado | `EtiquetaEstado`: el color transiciona y el texto se cruza con fundido |
| RF-31, error y progreso animados | `CampoTextoValidado` y `BotonGuardar` |

Falta **RF-30**, la aparición animada de los ítems del listado: depende de que exista el listado real
de la persona 1. Se agrega sobre su `LazyColumn` cuando esté.
