# CitaNova — sistema de citas médicas

CitaNova es una aplicación para consultar médicos, administrar horarios y gestionar
citas médicas. El frontend está desarrollado con Angular y se comunica con el
backend Spring Boot ubicado en `../citas-backend`.

La interfaz utiliza la paleta Nord Blue de [`src/styles.css`](src/styles.css).
El diseño de [`welcome.html`](src/app/features/public/welcome/welcome.html) y
[`welcome.css`](src/app/features/public/welcome/welcome.css) sirve como referencia
para las pantallas de acceso, administración, médicos y pacientes.

La [documentación del frontend](docs/frontend.md) describe la estructura del código,
el funcionamiento de los módulos y los estilos compartidos.

## Requisitos

El entorno local requiere las siguientes herramientas:

- Node.js compatible con Angular 22: `^22.22.3`, `^24.15.0` o `>=26.0.0`.
- npm y las dependencias definidas en `package-lock.json`.
- Java 21 o superior para el backend, que incluye el wrapper de Maven.
- MariaDB en el puerto 3306, con la base `citas_db` y un usuario con acceso.

## Ejecución local

La conexión de MariaDB se configura en
`../citas-backend/src/main/resources/application.properties`. Las variables
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y
`SPRING_DATASOURCE_PASSWORD` permiten sustituir esa configuración.

El backend se inicia desde su directorio:

```bash
cd ../citas-backend
./mvnw spring-boot:run
```

La API atiende en `http://localhost:8081/api/v1`. Hibernate actualiza las tablas y
el inicializador crea los roles principales con sus permisos cuando todavía no existen.

El frontend se inicia en otra terminal, desde el directorio `citas-frontend`:

```bash
npm ci
npm start
```

La aplicación queda disponible en `http://localhost:4200`. La instalación con
`npm ci` solo resulta necesaria cuando las dependencias aún no están instaladas
o cuando cambia `package-lock.json`.

El puerto del frontend puede indicarse al iniciar el servidor:

```bash
npm start -- --port 4201
```

Los cambios de `angular.json` o `proxy.conf.json` requieren reiniciar el servidor
de Angular para que la nueva configuración entre en funcionamiento.

## Conexión con la API

Los servicios utilizan la base `/api/v1`, definida en
`src/app/core/config/api.config.ts`. Durante el desarrollo, `proxy.conf.json`
reenvía las solicitudes `/api/**` a Spring Boot y ajusta el origen para permitir
el acceso local desde los puertos del frontend. Un cambio de puerto del backend
requiere actualizar `target` y `headers.Origin` en ese archivo.

El interceptor adjunta el JWT a los recursos privados. El inicio de sesión y el
registro se realizan sin un token anterior. Una respuesta 401 de un recurso
privado elimina la sesión y dirige al login; una respuesta 403 dirige a la
pantalla de acceso denegado.

El proxy funciona con `npm start`. En producción, el servidor web debe reenviar
`/api/` al backend y resolver las rutas del frontend mediante `index.html`.

## Registro y paneles

La ruta `/register` crea una cuenta de paciente mediante `/api/v1/auth/register`.
La contraseña de registro debe tener entre 8 y 12 caracteres. Una vez creada la
cuenta, la aplicación vuelve al login.

Después de iniciar sesión, la cuenta accede a `/panels`. El selector presenta
las áreas disponibles según sus permisos, con independencia del nombre del rol.

| Área           | Ruta       | Funciones                                                     |
| -------------- | ---------- | ------------------------------------------------------------- |
| Administración | `/admin`   | Usuarios, roles, permisos, especialidades y médicos           |
| Médico         | `/doctor`  | Directorio médico, horarios, agenda del día y diagnóstico     |
| Paciente       | `/patient` | Directorio médico, horarios disponibles, reservas e historial |

Cada panel exige su permiso de acceso y al menos una función autorizada:

| Panel          | Permiso de acceso |
| -------------- | ----------------- |
| Administración | `ACCESO_ADMIN`    |
| Médico         | `ACCESO_MEDICO`   |
| Paciente       | `ACCESO_PACIENTE` |

El catálogo `src/app/core/navigation/panels.config.ts` define los requisitos de
las funciones y se comparte entre el selector, los menús y el guard de paneles.
Un rol personalizado puede tener acceso a varios paneles. El permiso de acceso
no concede sus acciones: cada función también exige sus propios permisos.

El enlace «Cambiar de panel» permite regresar al selector desde las pantallas
privadas. Una cuenta sin paneles autorizados recibe un mensaje con la indicación
de solicitar la revisión de sus permisos.

Las reglas de acceso y los casos particulares se describen en la
[documentación del frontend](docs/frontend.md#acceso-y-permisos).
Los cambios de permisos requieren un nuevo inicio de sesión para actualizar
los datos almacenados en la cuenta.

## Actualización de una base existente

El inicializador conserva los roles existentes y los permisos que se hayan
revocado. Las instalaciones anteriores pueden incorporar los nuevos accesos
mediante la migración
`../citas-backend/database/migrations/2026-10-09-panel-access.sql`.

La actualización se realiza con el siguiente orden:

1. El backend anterior se detiene y se inicia la versión que reconoce los
   permisos `ACCESO_*`.
2. La migración se ejecuta una vez sobre la base correspondiente. Desde el
   directorio del backend, el comando de ejemplo es:

   ```bash
   mariadb --host=127.0.0.1 --user=citas_user --password citas_db < database/migrations/2026-10-09-panel-access.sql
   ```

   El usuario y el nombre de la base deben corresponder a la configuración local.
   La contraseña se solicita sin incluirse en el comando. El script añade el
   acceso correspondiente a `ROLE_ADMIN`, `ROLE_DOCTOR` y `ROLE_PATIENT`, además
   de los permisos administrativos para activar y desactivar médicos.

3. La sesión se cierra y se inicia nuevamente. Los accesos de los roles
   personalizados se asignan desde la pantalla Roles junto con sus funciones.

La migración conserva las asignaciones existentes y no modifica los roles
personalizados. Su repetición inmediata no duplica permisos, pero su ejecución
no debe programarse en cada arranque, porque repondría permisos revocados después.

## Activación y desactivación

Los listados administrativos ofrecen «Activar» o «Desactivar» de acuerdo con
el estado del registro y los permisos de la cuenta.

| Registro     | Permiso de activación       | Permiso de desactivación  |
| ------------ | --------------------------- | ------------------------- |
| Usuario      | `ACTUALIZAR_USUARIOS`       | `DESACTIVAR_USUARIOS`     |
| Médico       | `ACTUALIZAR_MEDICOS`        | `ELIMINAR_MEDICOS`        |
| Especialidad | `ACTUALIZAR_ESPECIALIDADES` | `ELIMINAR_ESPECIALIDADES` |

El acceso también requiere `ACCESO_ADMIN` y el permiso de consulta del recurso.
Estas operaciones conservan los registros y sus relaciones; modifican únicamente
su estado y no desactivan recursos asociados en cascada.

La reactivación de especialidades utiliza
`PATCH /api/v1/especialidades/{id}/estado` con `{ "activo": true }`.
Los roles eliminados y las citas canceladas no se reactivan desde estos botones.

## Datos de desarrollo

El archivo `../citas-backend/database/data.sql` contiene cuentas de ejemplo:

| Cuenta        | Correo                | Contraseña de ejemplo |
| ------------- | --------------------- | --------------------- |
| Administrador | `admin@clinica.com`   | `123456`              |
| Médico        | `doctor@clinica.com`  | `123456`              |
| Paciente      | `paciente@correo.com` | `123456`              |

En una base nueva, el primer arranque del backend crea los roles con sus permisos.
Después puede cargarse `data.sql` para incorporar las cuentas y los catálogos
de ejemplo. La creación de los roles mediante SQL antes del primer arranque
los deja sin los permisos del inicializador, ya que este conserva los roles existentes.

Estas cuentas pertenecen al entorno de desarrollo y sus datos pueden haber
cambiado en la instalación local. La longitud de sus contraseñas de ejemplo
no corresponde al requisito del registro de cuentas nuevas.

## Verificación y compilación

Las pruebas del frontend se ejecutan con:

```bash
npm test -- --watch=false
```

La suite verifica rutas, paneles y acciones por permisos, el inicio de sesión,
el registro público, el JWT, la agenda y la actualización de las pantallas al
recibir respuestas de la API. Las operaciones HTTP utilizan el backend de
pruebas de Angular y no crean registros en la base de desarrollo.

La compilación de producción se genera con:

```bash
npm run build
```

Los archivos generados se almacenan en `dist/citas-fronted`.
Las pruebas del backend se ejecutan desde su directorio con `./mvnw test`.
