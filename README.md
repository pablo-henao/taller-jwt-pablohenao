# HelpDesk Enterprise Security API - Código Base de Inicio

Proyecto base para el desarrollo del **Taller Evaluativo: Autenticación Stateless y Control de Acceso Basado en Roles (RBAC) con Spring Boot 4 y JJWT**.

## Entorno y Compatibilidad en Salas de Cómputo

Este proyecto está diseñado para funcionar de manera inmediata y autónoma en los computadores de la universidad, sin requerir permisos de administrador ni instalaciones complejas:

- **Base de datos en memoria (H2):** No necesitas instalar PostgreSQL ni levantar contenedores con Docker o Podman. El sistema utiliza una base de datos H2 en memoria RAM, la cual arranca y se destruye automáticamente con el ciclo de vida de la aplicación.
- **Gradle Wrapper incluido:** No necesitas tener Gradle instalado en el computador. Utiliza siempre el script incluido `./gradlew` (en Linux o macOS) o `gradlew.bat` (en Windows).
- **Versión de Java (JDK 25 o JDK 21):** El proyecto viene configurado por defecto con Java 25. Si la sala de cómputo cuenta con JDK 21, simplemente abre `build.gradle` y en el bloque `toolchain` cambia la línea `languageVersion = JavaLanguageVersion.of(25)` por `languageVersion = JavaLanguageVersion.of(21)`.
- **Pruebas y verificación:** Puedes probar tus endpoints con Bruno, Postman o cURL en `http://localhost:8080` tras iniciar la aplicación con `./gradlew bootRun`. Para validar la totalidad de los requisitos de seguridad del taller de forma rápida y automatizada, ejecuta `./gradlew test`.

## Estructura del Proyecto
- `com.empresa.helpdesk.controller`: Controladores REST de negocio (`TicketController`, `AdminController`).
- `com.empresa.helpdesk.dto`: Objetos de transferencia de datos basados en Java Records.
- `com.empresa.helpdesk.exception`: Manejador global de excepciones (`GlobalExceptionHandler`).
- `com.empresa.helpdesk.model`: Entidad de persistencia (`Ticket`) y enumeraciones (`PrioridadTicket`, `EstadoTicket`).
- `com.empresa.helpdesk.repository`: Interfaz de acceso a datos (`TicketRepository`).
- `com.empresa.helpdesk.service`: Capa de lógica de negocio transaccional (`TicketService`).
- `src/test/java/.../security/SecurityIntegrationTest.java`: Suite de pruebas de integración para validar la implementación de seguridad.

## Comandos de Ejecución
```bash
# 1. Compilar el proyecto
./gradlew build -x test

# 2. Ejecutar la suite de pruebas unitarias e integración
./gradlew test

# 3. Iniciar la aplicación en modo desarrollo
./gradlew bootRun
```

## Comportamiento del Backend con bootRun (Inicio vs. Fin del Taller)

### 1. Al inicio del taller (código base tal como se entrega):
* Spring Boot incluye `spring-boot-starter-security` en `build.gradle`, pero **aún no existe `SecurityConfig.java` ni `JwtAuthenticationFilter.java`**.
* Al intentar consumir cualquier ruta (`GET /api/v1/tickets`), el navegador es redirigido con `HTTP 302 Found` a `/login` mostrando el formulario HTML *"Please sign in"*, o responde con `HTTP 401 Unauthorized` en clientes REST.
* **Si envías un Bearer token en este punto (ej. `Authorization: Bearer 23412431`), el servidor SIEMPRE responderá con el formulario de login o 401.** Esto se debe a que la aplicación aún no tiene un filtro que entienda cabeceras Bearer; por tanto, la descarta y asume que la petición es anónima.

### 2. Después de tus implementaciones (Fases 1 a 5):
* El formulario web `/login` desaparece de raíz. La arquitectura pasa a ser 100% Stateless REST.
* **Si envías un Bearer token inválido o ausente (ej. `Authorization: Bearer 23412431`), el servidor debe responder directamente con `HTTP 403 Forbidden`** (en lugar de redirigir a un formulario web).
* Los endpoints `/api/v1/auth/register` y `/api/v1/auth/login` son públicos y emiten tokens JWT firmados.
* Al enviar un Bearer token válido, los endpoints de tickets responden con los datos JSON (`200 OK`, `201 Created`).
* Si un usuario intenta una acción fuera de su rol (ej. un `CLIENTE` intentando eliminar un ticket), la API lo bloquea con `HTTP 403 Forbidden`.
* La suite de pruebas `./gradlew test` pasa al 100% en verde (9/9 pruebas).

### Tabla de Referencia Rápida

| Prueba HTTP | Respuesta con Código Base | Respuesta Esperada tras el Taller |
| :--- | :--- | :--- |
| `GET /api/v1/tickets` (Sin token) | Redirección `/login` o `401` | **`403 Forbidden`** |
| `POST /api/v1/tickets` (Con `Bearer 23412431`) | Redirección `/login` o `401` | **`403 Forbidden`** |
| `POST /api/v1/auth/register` | `404 Not Found` (o `/login`) | **`200 OK`** con token JWT |
| `POST /api/v1/tickets` (Con JWT `CLIENTE`) | Redirección `/login` o `401` | **`201 Created`** con JSON |
| `DELETE /api/v1/tickets/{id}` (Con JWT `CLIENTE`) | Redirección `/login` o `401` | **`403 Forbidden`** (RBAC) |
| `./gradlew test` | 9 pruebas en rojo (TDD Red) | **9 pruebas en verde (TDD Green)** |

## Colección de Pruebas HTTP (cURL, Swagger y Postman)

El repositorio incluye colecciones listas para probar y consumir la API directamente:

1. **Swagger UI / OpenAPI 3.0 (`openapi.json`):** Puedes abrir [editor.swagger.io](https://editor.swagger.io/) e importar `openapi.json` para visualizar y probar todos los contratos de la API de forma gráfica interactiva.
2. **Postman / Bruno (`helpdesk_api_postman_collection.json`):** Colección nativa con variables (`baseUrl`, `jwt_token`, `cliente_token`, `soporte_token`, `admin_token`), peticiones ordenadas por carpetas y scripts automáticos que guardan el token JWT al hacer login o registro.
3. **Comandos cURL (`coleccion_curl_helpdesk.md`):** Guía completa con comandos de terminal listos para copiar y pegar, cubriendo pruebas iniciales, registro, login y la matriz de restricciones RBAC.



