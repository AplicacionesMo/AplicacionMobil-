# Plan de equipo

Entrega: **miércoles 14-10-2026**, antes de la primera sesión de la Evaluación Parcial 3.

## Calendario

| Cuándo | Qué |
|---|---|
| Domingo 11 | Cimiento del proyecto. Nadie más programa hasta que esté en la rama principal |
| Lunes 12 | Las tres verticales en paralelo |
| Martes 13 | Terminar, animaciones, integración y pruebas conjuntas |
| Miércoles 14 temprano | README final, prueba completa, congelar el repositorio, entrega en AVA |

## Reparto

### Cimiento (antes del reparto, común a los tres)

Paquetes, dependencias, enum del dominio, entidades de Room, DAO, base de datos, repositorios,
navegación con pantallas provisionales, tema visual y el componente compartido de campo de
formulario validado.

Los DAO declaran **todas** las consultas que necesitan los tres, incluida la que cuenta las guías de
un ticket: sobre ella se apoya la regla de que no se puede cerrar un ticket sin guía emitida.

### Persona 1 — Tickets

1. ViewModel del listado.
2. Pantalla de listado: número, título, cliente, fecha y etiqueta de estado con su color.
3. ViewModel del formulario de ticket, con los errores por campo.
4. Formulario de ticket y de equipo (formularios 1 y 2 de 5).
5. Guardado con correlativo y fecha automáticos.
6. Edición del ticket, bloqueada si está cerrado o cancelado.

### Persona 2 — Estados y guías

1. ViewModel del detalle de ticket.
2. Pantalla de detalle, con sus guías asociadas.
3. Cambio de estado entre los siete estados intermedios.
4. Formulario de cierre (formulario 3 de 5), que exige al menos una guía emitida.
5. Formulario de cancelación (formulario 4 de 5), con motivo obligatorio.
6. Solo lectura para tickets cerrados y cancelados.
7. ViewModel y formulario de guía (formulario 5 de 5).
8. Detalle de la guía.
9. Integrar los componentes de cámara y ubicación de la persona 3.

### Persona 3 — Recursos nativos, animaciones y documentación

1. Permisos en el manifiesto y `FileProvider` configurado. **Lo primero del lunes.**
2. Componente de cámara independiente, que entrega la URI de la foto.
3. Componente de ubicación independiente, que entrega latitud y longitud.
4. Pantalla de prueba propia para ambos.
5. Manejo del permiso denegado, con mensaje explicativo y acceso a ajustes.
6. Galería de fotos de la guía.
7. Entregar los componentes a la persona 2.
8. Animaciones de toda la aplicación (martes, cuando las pantallas existan).
9. README final con capturas.

## Reglas de trabajo

**Una rama por tarea**, nombrada por lo que hace: `tickets/listado`, `guias/cierre`,
`nativos/camara`. Nada se programa directo sobre la rama principal.

**Un *pull request* por rama, revisado por otro integrante** antes de mezclar. La revisión cruzada
es la evidencia de participación de los tres.

**Cada integrante sube sus propios commits.** Nadie commitea el trabajo de otro: el historial es lo
que acredita la participación individual.

**Dueño único de los archivos compartidos.** Quien necesite un cambio ahí lo pide, no lo edita:

| Archivo | Dueño |
|---|---|
| `build.gradle` (todas las dependencias ya están declaradas) | Persona 1 |
| Entidades, DAO y base de datos | Persona 1 |
| Grafo de navegación | Persona 1 |
| Tema y colores | Persona 1 |
| Componente de campo de formulario validado | Persona 1 |

Cada integrante reemplaza el contenido de **su propia** pantalla provisional; nadie toca el grafo de
navegación.

**Los componentes de cámara y ubicación se desarrollan como piezas independientes** que devuelven un
valor hacia afuera. Así la persona 2 no queda esperando a la persona 3, ni editan el mismo archivo.

## Antes de entregar

- [ ] Los cinco formularios existen y validan: ticket, equipo, guía, cierre, cancelación.
- [ ] Ninguna validación quedó dentro de un *composable*.
- [ ] Flujo completo probado: crear ticket → avanzar estados → emitir guía con foto y ubicación → cerrar.
- [ ] El cierre se bloquea cuando el ticket no tiene guías.
- [ ] Los datos sobreviven al cierre y reapertura de la aplicación.
- [ ] Los tres integrantes tienen commits propios en el historial.
- [ ] README completo, con nombres, número de equipo y pasos para ejecutar.
- [ ] Repositorio congelado: **cualquier cambio posterior a la entrega implica 1.0 automático,
      grupal e individual.**
- [ ] Código fuente subido al enlace grupal de AVA.
- [ ] Los tres activaron su enlace individual de AVA.
