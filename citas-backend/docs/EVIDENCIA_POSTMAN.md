# Evidencia de ejecución de servicios - Postman

Colección incluida en el repositorio:

`citas-backend/postman/Sistema_Citas_Medicas_Fase1.postman_collection.json`

## Flujo probado

1. **Autenticación**
   - `POST /api/v1/auth/login`
   - Resultado: `200 OK`
   - La colección guarda automáticamente el JWT en la variable `token`.

2. **Prueba de backend protegido**
   - `GET /api/hola`
   - Resultado con Bearer Token: `200 OK`.

3. **Médicos**
   - `GET /api/v1/doctors`
   - Resultado: `200 OK`.
   - `POST /api/v1/doctors`
   - Resultado: creación exitosa y persistencia en MySQL.

4. **Horarios disponibles**
   - `GET /api/v1/schedules/available?medicoId=1&fecha=2026-08-24`
   - Resultado: `200 OK`.
   - Se verificó que un horario registrado en MySQL fuera devuelto por el servicio.

5. **Citas médicas**
   - `POST /api/v1/appointments`
   - Resultado al reservar un horario disponible: `201 Created`.
   - Segundo intento sobre el mismo horario: `409 Conflict`.
   - `GET /api/v1/appointments`: `200 OK`.
   - Intento con un horario inexistente: `404 Not Found`.

## Uso de la colección

1. Levantar el backend en `http://localhost:8081`.
2. Importar la colección Postman v2.1 incluida en este repositorio.
3. Ejecutar primero `03 - Autenticación / Iniciar sesión`.
4. El script Post-response guardará el JWT en `{{token}}`.
5. Ejecutar las demás peticiones; la colección utiliza `Bearer {{token}}` para los endpoints protegidos.

## Seguridad de la colección

El valor compartido de `token` se encuentra vacío. La colección no contiene ningún JWT real exportado.
