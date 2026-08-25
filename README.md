# Sistema de Citas Médicas - Fase 1

Backend desarrollado con Spring Boot para la gestión inicial de citas médicas, médicos, horarios disponibles y autenticación con JWT.

## Entregables de la fase 1

- **Diagrama entidad-relación:** `citas-backend/docs/DER.md`
- **Backend Spring Boot:** carpeta `citas-backend/`
- **Colección Postman v2.1:** `citas-backend/postman/Sistema_Citas_Medicas_Fase1.postman_collection.json`
- **Evidencia y guía de pruebas:** `citas-backend/docs/EVIDENCIA_POSTMAN.md`

## Seguridad

El backend utiliza Spring Security con autenticación JWT. El endpoint de login es público y los demás endpoints REST requieren un token Bearer válido.

### Login

`POST /api/v1/auth/login`

Ejemplo:

```json
{
  "email": "paciente@correo.com",
  "password": "123456"
}
```

La colección de Postman guarda automáticamente el JWT recibido en la variable local `token` y lo utiliza como Bearer Token en las peticiones protegidas.

## Endpoints principales

- `GET /api/hola`
- `GET /api/v1/doctors`
- `POST /api/v1/doctors`
- `GET /api/v1/schedules/available?medicoId=1&fecha=2026-08-24`
- `POST /api/v1/auth/login`
- `GET /api/v1/appointments`
- `POST /api/v1/appointments`

## Ejecución

Desde `citas-backend/`:

```bash
mvnw.cmd spring-boot:run
```

Puerto configurado: `8081`.

Base URL usada por Postman:

```text
http://localhost:8081
```

## Validaciones realizadas

- Compilación Maven: `BUILD SUCCESS`.
- Login JWT: `200 OK`.
- Listado de médicos: `200 OK`.
- Creación de médico: respuesta exitosa y persistencia en MySQL.
- Horarios disponibles: `200 OK`.
- Creación de cita: `201 Created`.
- Intento de reservar nuevamente el mismo horario: `409 Conflict`.
- Listado de citas: `200 OK`.
- Intento de crear cita con horario inexistente: `404 Not Found`.
