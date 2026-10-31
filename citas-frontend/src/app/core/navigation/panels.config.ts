/** El identificador distingue las tres áreas disponibles en la aplicación. */
export type PanelId = 'admin' | 'doctor' | 'patient';

/** La definición relaciona una función del menú con su ruta y todos los permisos que exige. */
export interface PanelFunction {
  id: string;
  label: string;
  description: string;
  route: string;
  permissions: readonly string[];
  fragment?: string;
}
/** La definición agrupa las funciones del panel y el permiso necesario para acceder a él. */
export interface PanelDefinition {
  id: PanelId;
  label: string;
  title: string;
  description: string;
  route: string;
  functions: readonly PanelFunction[];
  accessPermission: string;
}

/** El catálogo define los paneles, sus funciones y sus permisos. Cada panel requiere acceso explícito y al menos una función autorizada. */
export const PANELS: readonly PanelDefinition[] = [
  {
    id: 'admin',
    label: 'Administración',
    title: 'Panel Administrativo',
    route: '/admin',
    accessPermission: 'ACCESO_ADMIN',
    description: 'Usuarios, roles, especialidades y perfiles médicos.',
    functions: [
      {
        id: 'statistics',
        label: 'Estadísticas generales',
        description: 'Consulta los totales de usuarios, médicos, citas del mes y roles.',
        route: '/admin/statistics',
        permissions: ['VER_USUARIOS', 'VER_MEDICOS', 'VER_ROLES', 'VER_CITAS'],
      },
      {
        id: 'users',
        label: 'Listar usuarios',
        description: 'Consulta los usuarios del sistema.',
        route: '/admin/users',
        permissions: ['VER_USUARIOS'],
      },
      {
        id: 'newUser',
        label: 'Crear usuario',
        description: 'Registra un nuevo usuario.',
        route: '/admin/users/new',
        permissions: ['CREAR_USUARIOS'],
      },
      {
        id: 'roles',
        label: 'Listar roles',
        description: 'Consulta los roles y sus permisos.',
        route: '/admin/roles',
        permissions: ['VER_ROLES'],
      },
      {
        id: 'newRole',
        label: 'Crear rol',
        description: 'Registra un rol personalizado.',
        route: '/admin/roles/new',
        permissions: ['CREAR_ROLES'],
      },
      {
        id: 'specialties',
        label: 'Listar especialidades',
        description: 'Consulta las especialidades médicas.',
        route: '/admin/specialties',
        permissions: ['VER_ESPECIALIDADES'],
      },
      {
        id: 'newSpecialty',
        label: 'Crear especialidad',
        description: 'Registra una especialidad médica.',
        route: '/admin/specialties/new',
        permissions: ['CREAR_ESPECIALIDADES'],
      },
      {
        id: 'doctors',
        label: 'Listar médicos',
        description: 'Consulta los perfiles médicos.',
        route: '/admin/doctors',
        permissions: ['VER_MEDICOS'],
      },
      {
        id: 'newDoctor',
        label: 'Crear médico',
        description: 'Asocia un usuario y una especialidad a un perfil médico.',
        route: '/admin/doctors/new',
        permissions: ['CREAR_MEDICOS', 'VER_USUARIOS', 'VER_ESPECIALIDADES'],
      },
    ],
  },
  {
    id: 'doctor',
    label: 'Médico',
    title: 'Panel del Médico',
    route: '/doctor',
    accessPermission: 'ACCESO_MEDICO',
    description: 'Directorio de médicos, horarios, agenda y diagnóstico.',
    functions: [
      {
        id: 'doctors',
        label: 'Listar médicos',
        description: 'Consulta el directorio de médicos.',
        route: '/doctor/doctors',
        permissions: ['VER_MEDICOS'],
      },
      {
        id: 'schedules',
        label: 'Crear horario',
        description: 'Registra un horario de atención.',
        route: '/doctor/schedules',
        permissions: ['CREAR_HORARIOS'],
      },
      {
        id: 'appointments',
        label: 'Ver citas',
        description: 'Consulta tu agenda del día.',
        route: '/doctor/appointments',
        permissions: ['VER_CITAS'],
      },
      {
        id: 'diagnosis',
        label: 'Registrar diagnóstico',
        description: 'Registra el diagnóstico y la receta de una cita.',
        route: '/doctor/diagnosis',
        permissions: ['REGISTRAR_DIAGNOSTICO'],
      },
    ],
  },
  {
    id: 'patient',
    label: 'Paciente',
    title: 'Panel del Paciente',
    route: '/patient',
    accessPermission: 'ACCESO_PACIENTE',
    description: 'Médicos, horarios disponibles, reservas e historial.',
    functions: [
      {
        id: 'doctors',
        label: 'Listar médicos',
        description: 'Encuentra un profesional para tu consulta.',
        route: '/patient/doctors',
        permissions: ['VER_MEDICOS'],
      },
      {
        id: 'schedules',
        label: 'Horarios disponibles',
        description: 'Consulta los horarios de atención.',
        route: '/patient/schedules',
        permissions: ['VER_HORARIOS_DISPONIBLES'],
      },
      {
        id: 'newAppointment',
        label: 'Crear cita',
        description: 'Reserva una cita médica.',
        route: '/patient/appointment/new',
        permissions: ['CREAR_CITA'],
      },
      {
        id: 'history',
        label: 'Historial de citas',
        description: 'Consulta el historial de tus citas.',
        route: '/patient/history',
        permissions: ['VER_HISTORIAL'],
      },
    ],
  },
];
