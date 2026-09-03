# Colección de Peticiones cURL, Postman y OpenAPI: Helpdesk Security API

Guía de pruebas HTTP para el **Taller Evaluativo: Autenticación Stateless y Control de Acceso Basado en Roles (RBAC) con Spring Boot 4 y JJWT**.

Esta colección permite probar los endpoints desde tres vías diferentes:
1. **Importación en Swagger Editor / Swagger UI:** Utilizando el archivo `openapi.json` incluido en el proyecto.
2. **Importación en Postman / Bruno / Insomnia:** Importando la colección nativa `helpdesk_api_postman_collection.json` o el archivo `openapi.json`.
3. **Consola de Comandos (cURL):** Ejecutando directamente las instrucciones en la terminal de tu sistema operativo.

---

## 1. Instrucciones de Importación en Herramientas

### En Swagger UI / Swagger Editor
1. Abre [editor.swagger.io](https://editor.swagger.io/) en tu navegador.
2. Haz clic en el menú **File -> Import file**.
3. Selecciona el archivo `openapi.json` del proyecto.
4. Tendrás acceso inmediato a la documentación interactiva y al botón **Authorize** para ingresar el token JWT (`Bearer <tu_token>`).

### En Postman
1. Abre Postman y pulsa el botón **Import** (esquina superior izquierda).
2. Arrastra y suelta el archivo `helpdesk_api_postman_collection.json` (o `openapi.json`).
3. La colección ya cuenta con variables preconfiguradas (`baseUrl`, `jwt_token`, `cliente_token`, `soporte_token`, `admin_token`, `ticket_id`) y scripts automáticos que guardan el token JWT al ejecutar las peticiones de registro o login.

---

## 2. Comandos cURL Listos para Ejecución

### Fase 0: Verificación Inicial del Backend (Al inicio del taller sobre el código base)

Al arrancar el código base inicial con `./gradlew bootRun`, Spring Boot detecta la dependencia `spring-boot-starter-security` en `build.gradle` pero aún no cuenta con `SecurityConfig.java`. En consecuencia, activa su autoconfiguración predeterminada con formulario web de inicio de sesión y autenticación básica:

```bash
# 1. Consultar listado general de tickets desde terminal
curl -i -X GET http://localhost:8080/api/v1/tickets
```
* **Respuesta en terminal (cURL / Postman sin redirects):** `HTTP/1.1 401 Unauthorized` con cabecera `WWW-Authenticate: Basic realm="Realm"`.
* **Respuesta en navegador web (o clientes con redirects activos):** Redirección `HTTP/1.1 302 Found` hacia `http://localhost:8080/login`, desplegando el formulario HTML predeterminado de Spring Security (*"Please sign in"*).

> **Nota pedagógica fundamental:** Este bloqueo es el comportamiento esperado al arrancar `src_base`. Confirma que el servidor está activo y que Spring Security intercepta el tráfico. El objetivo del taller consiste en reemplazar este formulario web y las sesiones por la arquitectura Stateless con JWT y control de acceso RBAC.

```bash
# 2. Consultar panel de auditoría sin autenticación
curl -i -X GET http://localhost:8080/api/v1/admin/auditoria
```
* **Respuesta al inicio del taller:** `HTTP/1.1 401 Unauthorized` o redirección a `/login`.
* **Respuesta tras completar el taller (con Bearer Token de ADMIN):** `HTTP/1.1 200 OK` con el JSON de auditoría del sistema.
* **Respuesta tras completar el taller (sin token o con rol CLIENTE):** `HTTP/1.1 403 Forbidden` (bloqueo RBAC directo sin formulario).


---

### Fase 1: Autenticación y Registro de Usuarios (Endpoints Públicos)

#### 1. Registrar usuario con rol CLIENTE
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente_demo",
    "password": "password123"
  }'
```
*Respuesta esperada:* `HTTP/1.1 200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 2. Registrar usuario con rol SOPORTE
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register-soporte \
  -H "Content-Type: application/json" \
  -d '{
    "username": "soporte_laura",
    "password": "password123"
  }'
```
*Respuesta esperada:* `HTTP/1.1 200 OK` con token JWT.

#### 3. Registrar usuario con rol ADMIN
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_root",
    "password": "admin123"
  }'
```
*Respuesta esperada:* `HTTP/1.1 200 OK` con token JWT.

#### 4. Iniciar Sesión (Login) con credenciales válidas
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente_demo",
    "password": "password123"
  }'
```
*Respuesta esperada:* `HTTP/1.1 200 OK` con token JWT firmado.

#### 5. Iniciar Sesión con credenciales erróneas (Fallo controlado)
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente_demo",
    "password": "password_incorrecto"
  }'
```
*Respuesta esperada:* `HTTP/1.1 401 Unauthorized`
```json
{
  "timestamp": "2026-09-03T...",
  "error": "UNAUTHORIZED",
  "mensaje": "Credenciales inválidas. Por favor verifica tu usuario o contraseña."
}
```

---

### Fase 2: Operaciones con Rol CLIENTE

*Nota: Define tu variable en la terminal sustituyendo `<TOKEN_CLIENTE>` por el token obtenido en el registro.*

```bash
# Exportar variable de entorno para las peticiones en la terminal
export TOKEN_CLIENTE="pega_aqui_el_token_de_cliente"
```

#### 1. Crear un nuevo ticket (Permitido para CLIENTE y ADMIN)
```bash
curl -i -X POST http://localhost:8080/api/v1/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_CLIENTE" \
  -d '{
    "titulo": "Falla en acceso a servidor de archivos",
    "descripcion": "El recurso compartido SMB no responde al autenticar.",
    "prioridad": "ALTA"
  }'
```
*Respuesta esperada:* `HTTP/1.1 201 Created`
```json
{
  "id": 1,
  "titulo": "Falla en acceso a servidor de archivos",
  "descripcion": "El recurso compartido SMB no responde al autenticar.",
  "prioridad": "ALTA",
  "estado": "ABIERTO",
  "creadorUsername": "cliente_demo",
  "tecnicoAsignado": null,
  "fechaCreacion": "2026-09-03T..."
}
```

#### 2. Consultar listado de tickets (Permitido para CLIENTE, SOPORTE y ADMIN)
```bash
curl -i -X GET http://localhost:8080/api/v1/tickets \
  -H "Authorization: Bearer $TOKEN_CLIENTE"
```
*Respuesta esperada:* `HTTP/1.1 200 OK`.

#### 3. Consultar ticket específico por ID
```bash
curl -i -X GET http://localhost:8080/api/v1/tickets/1 \
  -H "Authorization: Bearer $TOKEN_CLIENTE"
```
*Respuesta esperada:* `HTTP/1.1 200 OK`.

---

### Fase 3: Operaciones con Rol SOPORTE

```bash
# Exportar variable del token de soporte técnico
export TOKEN_SOPORTE="pega_aqui_el_token_de_soporte"
```

#### 1. Actualizar estado del ticket y asignar técnico (Permitido para SOPORTE y ADMIN)
```bash
curl -i -X PUT http://localhost:8080/api/v1/tickets/1/estado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_SOPORTE" \
  -d '{
    "estado": "EN_PROCESO",
    "tecnicoAsignado": "soporte_laura"
  }'
```
*Respuesta esperada:* `HTTP/1.1 200 OK`
```json
{
  "id": 1,
  "titulo": "Falla en acceso a servidor de archivos",
  "descripcion": "El recurso compartido SMB no responde al autenticar.",
  "prioridad": "ALTA",
  "estado": "EN_PROCESO",
  "creadorUsername": "cliente_demo",
  "tecnicoAsignado": "soporte_laura",
  "fechaCreacion": "2026-09-03T..."
}
```

---

### Fase 4: Operaciones con Rol ADMIN

```bash
# Exportar variable del token de administrador
export TOKEN_ADMIN="pega_aqui_el_token_de_admin"
```

#### 1. Consultar panel de auditoría (Exclusivo ADMIN)
```bash
curl -i -X GET http://localhost:8080/api/v1/admin/auditoria \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
*Respuesta esperada:* `HTTP/1.1 200 OK`
```json
{
  "sistema": "Helpdesk Enterprise Security API",
  "estado": "OPERACIONAL",
  "totalTickets": 1,
  "timestamp": "2026-09-03T..."
}
```

#### 2. Eliminar permanentemente un ticket (Exclusivo ADMIN)
```bash
curl -i -X DELETE http://localhost:8080/api/v1/tickets/1 \
  -H "Authorization: Bearer $TOKEN_ADMIN"
```
*Respuesta esperada:* `HTTP/1.1 204 No Content`.

---

### Fase 5: Validación de Restricciones RBAC (Pruebas Negativas de Seguridad)

Estas pruebas comprueban que el filtro y la matriz de seguridad interceptan las transacciones ilegales:

#### 1. CLIENTE intenta actualizar estado del ticket (Debe dar 403 Forbidden)
```bash
curl -i -X PUT http://localhost:8080/api/v1/tickets/1/estado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_CLIENTE" \
  -d '{
    "estado": "RESUELTO",
    "tecnicoAsignado": "cliente_demo"
  }'
```
*Respuesta esperada:* `HTTP/1.1 403 Forbidden`.

#### 2. CLIENTE intenta eliminar un ticket (Debe dar 403 Forbidden)
```bash
curl -i -X DELETE http://localhost:8080/api/v1/tickets/1 \
  -H "Authorization: Bearer $TOKEN_CLIENTE"
```
*Respuesta esperada:* `HTTP/1.1 403 Forbidden`.

#### 3. SOPORTE intenta eliminar un ticket (Debe dar 403 Forbidden)
```bash
curl -i -X DELETE http://localhost:8080/api/v1/tickets/1 \
  -H "Authorization: Bearer $TOKEN_SOPORTE"
```
*Respuesta esperada:* `HTTP/1.1 403 Forbidden`.

#### 4. CLIENTE intenta acceder al panel de auditoría (Debe dar 403 Forbidden)
```bash
curl -i -X GET http://localhost:8080/api/v1/admin/auditoria \
  -H "Authorization: Bearer $TOKEN_CLIENTE"
```
*Respuesta esperada:* `HTTP/1.1 403 Forbidden`.
