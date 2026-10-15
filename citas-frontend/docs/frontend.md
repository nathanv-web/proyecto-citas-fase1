# Documentación del frontend de CitaNova

CitaNova organiza la atención médica en tres áreas: administración, médicos y
pacientes. El frontend presenta los datos y valida los formularios; el backend
atiende las solicitudes y comprueba los permisos de cada operación.

La interfaz parte del diseño de bienvenida. Las demás pantallas comparten su
paleta, las tarjetas blancas, los encabezados azules, las esquinas redondeadas
y las sombras suaves.

## Estructura del proyecto

| Ubicación                         | Responsabilidad                                      |
| --------------------------------- | ---------------------------------------------------- |
| `src/main.ts`                     | Inicio de la aplicación Angular                      |
| `src/index.html`                  | Documento principal, idioma, título y metadatos      |
| `src/styles.css`                  | Paleta y estilos compartidos por las pantallas       |
| `src/app/app.*`                   | Contenedor principal y navegación privada            |
| `src/app/app.config.ts`           | Configuración del enrutador y del cliente HTTP       |
| `src/app/app.routes.ts`           | Rutas públicas, paneles y controles de acceso        |
| `src/app/core/auth`               | Modelos, servicio de sesión, guards e interceptor    |
| `src/app/core/config`             | Dirección base de la API                             |
| `src/app/core/navigation`         | Catálogo y selección de paneles autorizados          |
| `src/app/shared/panel-menu`       | Menú de funciones reutilizado por los tres paneles   |
| `src/app/shared/user-profile`     | Perfil personal y cierre de sesión en la cabecera   |
| `src/app/features/public`         | Página de bienvenida                                 |
| `src/app/features/auth`           | Inicio de sesión y acceso denegado                   |
| `src/app/features/panels`         | Selector de áreas disponibles                        |
| `src/app/features/admin`          | Usuarios, roles, especialidades y perfiles médicos   |
| `src/app/features/doctor`         | Agenda, creación de horarios y diagnóstico           |
| `src/app/features/patient`        | Directorio, horarios, reserva e historial            |
| `src/app/services/appointment.ts` | Servicio general de consultas y operaciones de citas |
| `public`                          | Imágenes y archivos públicos                         |
| `proxy.conf.json`                 | Reenvío local de solicitudes al backend              |

Los componentes utilizan sus plantillas HTML para presentar el contenido y sus
archivos TypeScript para gestionar los datos. Los estilos particulares se
mantienen en el CSS del componente. Las páginas de médicos y pacientes utilizan
las clases globales cuando su distribución no requiere reglas particulares.

Los paneles de administración, médico y paciente delegan la presentación de
sus funciones en `PanelMenu`. El selector utiliza el mismo diseño de tarjetas.

## Funcionamiento de los módulos

### Autenticación

`AuthService` envía las credenciales y guarda el token en `auth_token` y los datos
de la cuenta en `auth_user`, dentro de `localStorage`. El cierre de sesión elimina
ambas entradas. Los datos de sesión que no pueden interpretarse se descartan.

La cabecera privada muestra a la izquierda un icono circular y el nombre de la
cuenta. Al pulsarlo, `UserProfile` abre una tarjeta con el nombre completo, correo,
edad, teléfono y tipo de cuenta. La tarjeta se cierra con su botón, con Escape,
al pulsar fuera, al desplazar el foco fuera del perfil o al cambiar de ruta.
El botón **Salir** elimina la sesión y vuelve a la página Welcome (`/`).

El login actual entrega nombre, apellido, correo y roles. `LoginUsuario` admite
edad y teléfono como campos opcionales; cuando no están disponibles, el perfil
indica **No registrada** o **No registrado**. Actualmente el backend no almacena
la edad ni incluye el teléfono en la respuesta del login.

El login utiliza un formulario reactivo para validar el correo y la contraseña.
El registro reutiliza `UserForm` con el dato de ruta `publicRegistration` y crea
una cuenta de paciente. La misma pantalla administra usuarios cuando se abre
bajo las rutas de administración.

`authInterceptor` añade la cabecera `Authorization: Bearer ...` a las solicitudes
privadas de `/api/v1`. Los recursos de autenticación y las direcciones externas
quedan fuera de este tratamiento. El backend sigue siendo responsable de validar
el token y autorizar las operaciones.

### Administración

El módulo administrativo se carga mediante las rutas de `admin.routes.ts`.
Los listados de usuarios, médicos y especialidades presentan el estado de cada
registro y muestran sus acciones según los permisos de la cuenta.

La creación de médicos relaciona un usuario con una especialidad. La gestión de
roles permite definir su nombre y descripción, seleccionar permisos y asignar
roles a usuarios. Las casillas seleccionadas reciben un fondo azul claro para
facilitar la revisión de la selección.

### Área médica

La agenda consulta las citas del médico autenticado. La creación de horarios
recibe el identificador del médico, una fecha y las horas de inicio y fin.
El registro de diagnóstico utiliza el identificador de la cita, el diagnóstico
con su receta y las observaciones.

El acceso a la agenda y la autorización para registrar diagnósticos se evalúan
por separado. Cuando la cuenta solo tiene el permiso de diagnóstico, el formulario
recibe el ID de la cita sin consultar la agenda.

### Área del paciente

El directorio presenta el nombre, la especialidad y los datos profesionales de
cada médico. La consulta de disponibilidad recibe un ID Médico y una fecha.
Los horarios devueltos incluyen el ID Horario que utiliza el formulario de reserva.

La reserva exige un horario y un motivo de al menos cinco caracteres. El historial
presenta los datos de las citas y, cuando existen, su diagnóstico y observaciones.
La cancelación solo aparece para citas pendientes cuando la cuenta tiene el
permiso correspondiente, y mantiene la confirmación de la operación.

## Acceso y permisos

`PanelAccessService` interpreta el catálogo `PANELS`. Un panel requiere su permiso
`ACCESO_*` y al menos una función cuyos permisos estén completos. El nombre del rol
no determina las opciones de navegación.

| Control           | Comprobación                                            |
| ----------------- | ------------------------------------------------------- |
| `authGuard`       | Existencia de una sesión local                          |
| `panelGuard`      | Acceso al panel y a alguna de las funciones solicitadas |
| `permissionGuard` | Todos los permisos específicos declarados por la ruta   |

`VER_MEDICOS` es un permiso compartido, pero por sí solo no permite entrar a un
panel. Por ejemplo, `ACCESO_MEDICO` junto con `VER_MEDICOS` ofrece el directorio
en el área médica. La incorporación de `ACCESO_PACIENTE` también permite abrirlo
en el panel del paciente, mientras que administración continúa requiriendo
`ACCESO_ADMIN`.

La creación de un perfil médico exige `CREAR_MEDICOS`, `VER_USUARIOS` y
`VER_ESPECIALIDADES` para cargar sus selectores. La consulta de agenda utiliza
`VER_CITAS` y el registro de diagnóstico utiliza `REGISTRAR_DIAGNOSTICO`.
La consulta de historial utiliza `VER_HISTORIAL`; su botón de cancelación requiere
además `CANCELAR_CITA`.

El catálogo contiene las funciones implementadas. La asignación de un permiso
para una función sin pantalla no genera esa pantalla. La selección de un panel
tampoco crea un perfil de médico o paciente: el backend mantiene los requisitos
de identidad y propiedad de citas y horarios.

## Estilos compartidos

`src/styles.css` conserva los colores originales de la paleta Nord Blue. Las
variables `--app-*` son la referencia común del diseño. Los componentes utilizan
estas variables para evitar paletas diferentes entre pantallas.

| Variable              | Valor     | Uso principal                                |
| --------------------- | --------- | -------------------------------------------- |
| `--app-primary`       | `#356F9F` | Botones principales y enlaces                |
| `--app-primary-hover` | `#284D73` | Estado de interacción de botones             |
| `--app-primary-light` | `#DDECF6` | Degradados, iconos y selecciones             |
| `--app-secondary`     | `#5C91B8` | Detalles de la marca y bordes de interacción |
| `--app-accent`        | `#88BBD4` | Detalles sobre fondos oscuros                |
| `--app-navy`          | `#1B304B` | Cabecera privada y títulos                   |
| `--app-navy-light`    | `#284D73` | Separadores sobre fondos oscuros             |
| `--app-bg`            | `#F4F8FC` | Fondo general y campos                       |
| `--app-surface`       | `#FFFFFF` | Tarjetas y tablas                            |
| `--app-surface-alt`   | `#EAF2F9` | Cabeceras de tablas y notas                  |
| `--app-text`          | `#20374D` | Texto principal                              |
| `--app-text-muted`    | `#536B80` | Descripciones y ayudas                       |
| `--app-text-light`    | `#FFFFFF` | Texto sobre fondos oscuros                   |
| `--app-border`        | `#CFDFEA` | Bordes de campos, tarjetas y tablas          |
| `--app-success`       | `#327B71` | Mensajes correctos y registros activos       |
| `--app-warning`       | `#B77928` | Citas pendientes                             |
| `--app-danger`        | `#B74E60` | Errores, cancelaciones y desactivaciones     |
| `--app-info`          | `#356F9F` | Color informativo disponible                 |

Las esquinas utilizan `--app-radius` y `--app-radius-lg`, de 12 y 20 píxeles.
Las sombras se definen mediante `--app-shadow` y `--app-shadow-hover`.
Los fondos de estado mezclan el color correspondiente con la superficie blanca.
El texto de las etiquetas y los mensajes combina esos tonos con azul marino para
mantener un contraste adecuado sobre las superficies claras.

### Clases de uso común

| Clase                                                     | Función                                                 |
| --------------------------------------------------------- | ------------------------------------------------------- |
| `.app-brand`                                              | Marca con símbolo y nombre CitaNova                     |
| `.eyebrow`                                                | Etiqueta que identifica el área de la pantalla          |
| `.page`, `.dashboard`                                     | Ancho y separación general del contenido                |
| `.page-narrow`                                            | Ancho reducido para formularios de citas y horarios     |
| `.page-header`, `.dashboard-header`                       | Encabezado con título, descripción y fondo degradado    |
| `.btn-primary`, `.btn-secondary`, `.btn-danger`           | Acciones principales, secundarias y de cambio de estado |
| `.form-card`, `.roles-card`                               | Tarjetas para formularios y selección de roles          |
| `.form-grid`, `.field`, `.form-actions`                   | Distribución de campos y botones                        |
| `.record-grid`, `.record-card`, `.record-details`         | Tarjetas de médicos, horarios y citas                   |
| `.clinical-note`                                          | Diagnóstico y observaciones del historial               |
| `.table-container`                                        | Tabla con desplazamiento horizontal independiente       |
| `.status`, `.active`, `.inactive`, `.pending`             | Etiquetas de estado                                     |
| `.alert`, `.message`, `.loading`, `.empty-state`          | Mensajes y estados de la pantalla                       |
| `.permissions-grid`, `.permission-option`, `.role-option` | Selección de permisos y roles                           |

La bienvenida conserva sus nombres de clase en español. Sus variantes de botones
siguen la misma paleta de las pantallas privadas.

Los estilos compartidos se modifican en `styles.css`. El CSS de cada componente
solo contiene los ajustes que necesita esa pantalla, como el ancho de un
formulario o la distribución del inicio de sesión. Esta separación permite que
un cambio en botones, tablas o mensajes se aplique a todos los módulos.

## Adaptación y accesibilidad

Las pantallas utilizan encabezados y regiones de contenido para organizar la
lectura. Los campos disponen de etiquetas asociadas, y los mensajes administrativos
distinguen avisos de estado de errores mediante `role="status"` y `role="alert"`.

Los controles conservan un indicador visible al recibir foco. Las tablas
administrativas permiten desplazamiento mediante teclado y mantienen su ancho
dentro del contenedor. Los estados también presentan su nombre en texto, por lo
que su interpretación no depende únicamente del color.

Por debajo de 700 píxeles, los formularios pasan a una columna y sus acciones
se apilan. El login pasa a una columna por debajo de 800 píxeles. Los contenedores
de tarjetas ajustan automáticamente su cantidad de columnas al espacio disponible.
La bienvenida conserva sus ajustes de 950 y 480 píxeles.

La preferencia `prefers-reduced-motion` desactiva transiciones, animaciones y
desplazamiento suave cuando el navegador solicita movimiento reducido.

## Verificación

Las pruebas existentes comprueban los flujos y permisos con solicitudes HTTP
simuladas. La compilación comprueba TypeScript, las plantillas de Angular y los
presupuestos configurados en `angular.json`.

```bash
npm test -- --watch=false
npm run build
```

La revisión visual complementa estas comprobaciones. Las pantallas se revisan
con distintos anchos, incluyendo formularios, tablas y tarjetas con datos de
muestra. Los datos de esta revisión no requieren operaciones sobre la base real.
