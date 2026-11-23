# Microservicios TacticalDEV

| Proyecto | Puerto | Base MySQL | Responsabilidad |
|---|---|---|---|
| `ms-usuarios` | 8081 | `tactical_usuarios` | Login, roles (TECNICO / COORDINADOR), CRUD de usuarios |
| `ms-tickets` | 8082 | `tactical_tickets` | Tickets, equipos, cambios de estado e historial |
| `ms-guias` | 8083 | `tactical_guias` | Guías de servicio y sus fotos |

Spring Boot 3.3 · Java 17 · Spring Data JPA · MySQL 8 · Bean Validation · springdoc (Swagger).

## Ejecutar

1. Encender MySQL (XAMPP / Laragon / `docker compose up -d`). Las bases se crean solas.
2. En cada carpeta: `mvn spring-boot:run` o ejecutar la clase `Ms...Application` desde el IDE.
   Orden recomendado: usuarios, guías, tickets.
3. Swagger: `http://localhost:8081/swagger-ui.html` (8082, 8083).

Sin MySQL: perfil `h2` → `mvn spring-boot:run -Dspring-boot.run.profiles=h2`.

Variables opcionales: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASS`, `GUIAS_URL`.

## Usuarios de prueba (se crean al iniciar ms-usuarios por primera vez)

- `tecnico@tacticaldev.cl` / `tecnico123`
- `coordinador@tacticaldev.cl` / `coord123`

## Probar a mano (ejemplos)

```bash
curl -X POST localhost:8081/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"correo":"tecnico@tacticaldev.cl","clave":"tecnico123"}'

curl -X POST localhost:8082/api/v1/tickets -H "Content-Type: application/json" -d '{
  "titulo":"Equipo no enfria","descripcion":"Sala de servidores sobre 30 grados",
  "cliente":"Banco Uno","direccion":"Av. Providencia 1234","prioridad":"ALTA",
  "equipo":{"nombre":"Split muro","modelo":"LG 12000","serie":"ABC123","propiedad":"ARRENDADO"}}'

curl localhost:8082/api/v1/tickets
```
