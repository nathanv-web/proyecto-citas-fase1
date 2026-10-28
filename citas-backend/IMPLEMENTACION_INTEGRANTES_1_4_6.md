# Implementación de integrantes 1, 4 y 6

Se aplicaron las tareas obligatorias de las páginas 4, 7 y 9 del PDF. No se implementó recuperación por correo, marcada como fase 2 opcional. No se agregaron tests.

## Integración y estructura

`main` local incluye `origin/main` (`c1edb73`) y `origin/feature/appointments` (`97390bb`) mediante el merge `e1e6cc2`. No se realizó push. Los cambios posteriores permanecen en el directorio de trabajo para revisión.

Se conservaron la separación local `dto/request` y `dto/response`, las configuraciones locales y las adaptaciones previas de importaciones en tests existentes. Los DTO de cancelación y reprogramación siguen esta misma estructura. El trabajo original está respaldado en `stash@{0}` (`respaldo-local-antes-integracion-1-4-6`); no aplicar ese stash nuevamente sobre el trabajo actual.

## Integrante 1: seguridad y autenticación

- Las cuentas creadas desde administración reciben `debeCambiarPassword=true`. El registro público conserva `false`.
- El login informa ese indicador. `AutenticacionProvider` conserva el flujo de `AuthenticationManager` y aplica el bloqueo persistente.
- Tras cinco contraseñas incorrectas se bloquea el acceso un minuto. Cada cinco fallos adicionales, después de vencer el bloqueo, se duplica el periodo hasta 60 minutos. Un acceso correcto reinicia el contador. La contraseña actual incorrecta en cambio de contraseña también cuenta como fallo.
- Los mensajes de acceso no distinguen correo inexistente, cuenta inactiva, bloqueo o contraseña incorrecta. La auditoría registra resultado e identificador interno, sin contraseñas ni JWT.
- Los JWT consultan estado, bloqueo y permisos actuales. Una cuenta temporal solo puede consultar `/me` y cambiar su contraseña.
- `PUT /api/v1/me/password` verifica la contraseña actual y exige una nueva diferente, de 8 a 64 caracteres y hasta 72 bytes UTF-8 para BCrypt. Incrementa `versionSesion` e invalida sesiones anteriores; se requiere un nuevo login.
- Solo login y registro son públicos dentro de `/auth`.

### Base de datos

Para una base existente, respaldarla y ejecutar una sola vez `database/migrations/2026_10_27_seguridad_acceso.sql`. Incluye instrucciones SQL de reversión. Para instalaciones nuevas, `database/schema.sql` ya contiene las columnas. No ejecutar la migración después de crear una base con el esquema nuevo, porque las columnas ya existen. No se aplicó SQL sobre una base de datos real durante esta implementación.

### Contratos de acceso

- `POST /api/v1/auth/login`: `{ "email": "usuario@example.com", "password": "..." }`; respuesta conserva `token`, `tipo`, `usuario` y agrega `debeCambiarPassword`.
- `GET /api/v1/me`, con Bearer: `{ "usuario": { ... }, "debeCambiarPassword": false }`.
- `PUT /api/v1/me/password`, con Bearer: `{ "actual": "...", "nueva": "..." }`; éxito devuelve `message` y obliga a iniciar sesión otra vez.
- Operaciones restringidas durante cambio obligatorio: 403 con `code=PASSWORD_CHANGE_REQUIRED` y `message`.

## Integrante 4: autenticación y notificaciones

- Página standalone `/change-password`, acceso desde perfil y redirección obligatoria tras login o respuesta de API.
- Guards actualizan sesión y permisos mediante `/me`; comparten la consulta en curso.
- `NotificationService.success/error/warning/info(message)` y un solo `ToastContainer` global. Máximo cuatro avisos, cierre manual, autocierre a los seis segundos, pausa con foco o cursor y roles accesibles. Angular interpola el texto sin interpretarlo como HTML.
- El interceptor muestra `message` y los mensajes de `errors` de la API para cualquier respuesta HTTP fallida de todos los módulos, incluido login/registro. Los componentes no vuelven a mostrar el mismo error. Un 401 privado cierra sesión; un 403 de contraseña obligatoria redirige al cambio.
- Si no hubo respuesta HTTP, se identifica el fallo de conexión; si la API no envía `message`, se indica esa ausencia. Esos diagnósticos no se presentan como mensajes devueltos por la API.
- Confirmación reutilizable con `<dialog>`, foco inicial, Escape y botones antes de acciones críticas.
- Ionic también presenta errores globales de API mediante `ToastController`, sin repetir mensajes en sus páginas.

## Integrante 6: médicos y pacientes

- Directorio con búsqueda por nombre/correo, especialidad y estado enviados al backend, debounce y cancelación de consultas anteriores.
- Reserva por médico, fecha y horario disponible, sin escribir identificadores manualmente. La API confirma disponibilidad al guardar; un 409 actualiza los horarios.
- Historial con filtros de día o siete días, estados, motivo opcional de cancelación y confirmación.
- Reprogramación conserva el médico de la cita. La respuesta de citas agrega `idMedico` para consultar disponibilidad sin inferir identidad por nombre. Envía `{ "idHorarioNuevo": 10 }` a `PUT /appointments/{id}/reschedule`. Un 409 muestra el mensaje real y recarga horarios.
- Agenda del médico por día o semana usando `/appointments/my-agenda?fecha=...&periodo=dia|semana`, sin enviar identificadores de otros médicos. La autorización permanece en backend.

## Comprobaciones

- Backend: `./mvnw -q -Dmaven.test.skip=true compile` correcto.
- Angular: `npm run build` correcto, con la advertencia previa de CSS descrita abajo.
- Ionic: compilaciones de desarrollo y producción correctas, con advertencias previas de Browserslist y tamaño de estilos.
- Navegador con respuestas HTTP simuladas, sin archivos de tests nuevos: un único Toast para 400/401/403/404/409/500; detalles de validación; redirección obligatoria; cambio de contraseña con cierre de sesión; diálogo de reprogramación y conflicto 409 con recarga de horarios; historial a 375 px sin desbordamiento horizontal.
- Las comprobaciones de navegador verifican presentación e integración de contratos, no prueban transacciones reales ni concurrencia contra MariaDB. No se ejecutaron suites de tests durante la implementación inicial; véase la verificación posterior de abajo.

## Problemas previos detectados y no corregidos

1. **JWT identificado por correo mutable.** `JwtService` usa el correo como `subject` y `JwtAuthenticationFilter` busca la cuenta por ese correo. Si se cambia un correo y otra cuenta ocupa el anterior, un token previo puede identificar la nueva cuenta si las versiones de sesión coinciden. El diseño ya existía antes de estos cambios; se informa sin modificarlo.
2. **Historial de horarios pendiente en la rama integrada.** `database/migrations/2026_10_23_historial_citas.sql` está vacío y `model/Cita.java` mantiene `@OneToOne` para el horario, aunque la lógica nueva permite conservar citas canceladas y volver a reservar el mismo horario. Puede persistir o generarse una restricción de unicidad que impida esa reserva. Se necesita revisar el esquema real; no se cambió la relación ni se completó esa migración.
3. **Advertencia Angular existente.** `features/public/welcome/welcome.css` ocupa 6.33 kB, por encima del límite de aviso de 4 kB. La compilación termina correctamente; no se modificó ese CSS.
4. **Advertencias de dependencias/configuración Ionic existentes.** La instalación advierte sobre `uuid@7.0.3` y `@angular/platform-browser-dynamic`; la compilación advierte que Browserslist incluye navegadores fuera del soporte de Angular. También hay avisos de tamaño en `home.page.scss` (2.38 kB), `doctors.page.scss` (2.38 kB) y `schedules.page.scss` (3.29 kB), sobre un límite de aviso de 2 kB. No se actualizaron dependencias, estilos ni Browserslist.

## Verificación posterior del fallo de login

Se reprodujo `AuthLoginResponseTest.loginDebeDevolverTokenUsuarioRolesYPermisos`: esperaba `jwt-prueba` y recibía `null`. El test simulaba `JwtService.generateToken(String)`, pero el login utiliza `generateToken(UserDetails)` para incluir `versionSesion`. Se ajustó únicamente el argumento del mock al usuario autenticado; las aserciones se conservaron y no se agregaron tests.

- `./mvnw -q '-Dtest=*,!CitasBackendApplicationTests' test`: 114 tests, cero fallos y cero errores.
- `./mvnw -q -Dspring.jpa.hibernate.ddl-auto=validate test`: 115 tests, cero fallos, cero errores y cero omitidos. Incluye el contexto de aplicación con MariaDB; se validó el esquema sin actualizaciones automáticas.
- Arranque real mediante `spring-boot:run` con `spring.jpa.hibernate.ddl-auto=validate`: Tomcat inicia en el puerto 8081.
- HTTP real: `POST /api/v1/auth/login` con `{}` devuelve 400 y mensajes de validación de la API; `GET /api/v1/me` sin token devuelve 401.

La compilación inicial no acreditaba que los tests pasaran; esta verificación posterior añade esa evidencia. Los problemas previos documentados siguen sin corregirse.

## Ajuste posterior: selección sin introducir identificadores

En Angular se sustituyeron los tres campos que todavía pedían identificadores manuales: consulta de horarios por médico, creación de horario y selección de cita para diagnóstico. Ahora se muestran nombres, especialidad/correo y paciente/fecha/hora. La creación de horarios ofrece únicamente el perfil del médico autenticado. Las citas del selector de diagnóstico proceden de su propia agenda y solo se ofrecen las pendientes; al cambiar de cita se limpia el borrador anterior.

Los identificadores siguen viajando internamente en las llamadas existentes a la API. Las listas respetan los permisos actuales: consultar médicos requiere `VER_MEDICOS` y cargar la agenda requiere `VER_CITAS`; el backend devuelve 403 si faltan esos permisos y el interceptor presenta su mensaje. No se modificaron las autorizaciones del servidor.

El listado de roles ahora muestra `Permisos: +N`, calculado desde la respuesta, incluso `Permisos: +0`, sin enumerar los nombres. La edición de permisos conserva sus controles.

Verificación: `npm run build` correcto. En navegador, con respuestas HTTP simuladas, se comprobó la selección por nombre, el envío del médico/cita correspondiente, la exclusión de otros médicos al crear horarios, la exclusión de citas completadas al diagnosticar y los contadores +22/+0. No se encontraron campos para ingresar IDs en los formularios Angular; el único campo numérico manual restante es años de experiencia. Se comprobó diagnóstico a 375 px sin desbordamiento horizontal. No se crearon ni modificaron tests para este ajuste.
