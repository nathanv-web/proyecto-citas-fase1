package com.proyectocitas.service.Impl;

import com.proyectocitas.dto.request.UsuarioUpdateRequestDTO;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.service.AuditoriaService;
import java.util.HashSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    //Constructor para inyectar el repositorio de usuarios

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
    }

    //Obtener todos los usuarios registrados

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

  // =================================================
 // BUSCAR Y FILTRAR USUARIOS
// =================================================
// Permite buscar usuarios por nombre, apellido o correo.
// También puede filtrar opcionalmente por estado y rol.
@Override
public List<Usuario> buscarConFiltros(
        String search,
        Boolean activo,
        String rol) {

    // Limpiar espacios innecesarios en los parámetros de texto.
    String searchLimpio =
            search != null ? search.trim() : null;

    String rolLimpio =
            rol != null ? rol.trim() : null;

    // Delegar la búsqueda al repositorio.
    return usuarioRepository.buscarConFiltros(
            searchLimpio,
            activo,
            rolLimpio
    );
}

    //Buscar un usuario por su ID

    @Override
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                new ResourceNotFoundException("El usuario indicado no existe"
                ));
    }

    //Buscar un usuario por su correo

    @Override
    public Optional<Usuario> obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    //Guardar un nuevo usuario

    @Override
    public Usuario guardar(Usuario usuario) {
        usuario.setDebeCambiarPassword(true);

        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con este correo.");
        }

        usuario.setContraseña(
        passwordEncoder.encode(usuario.getContrasena())
        );
        return usuarioRepository.save(usuario);
    }

    //Actualizar los datos principales de un usuario existente

    @Override
    public Usuario actualizar(Long id,UsuarioUpdateRequestDTO usuario) {

    //Buscar el usuario existente en la base de datos
    Usuario usuarioDB = usuarioRepository.findById(id)
            .orElseThrow(()->
            new ResourceNotFoundException("El usuario Indicado no existe"));

    //actualizar nombre
        if (usuario.getNombre() != null) {
            usuarioDB.setNombre(usuario.getNombre());
        }
     //Actualizar apellido
            if (usuario.getApellido()!= null) {
                usuarioDB.setApellido(usuario.getApellido());
            }
            //Actualizar correo y verificar si no hay una duplicacion de correo
            if (usuario.getCorreo() != null) {
                if (!usuario.getCorreo().equalsIgnoreCase(usuarioDB.getCorreo())) {
                    if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
                        throw new DuplicateResourceException("Ya existe un usuario registrado con este correo");
                    }
                }
                usuarioDB.setCorreo(usuario.getCorreo());
            }

            //Actualizar telefono
            if (usuario.getTelefono() != null) {
            usuarioDB.setTelefono(usuario.getTelefono());
            }
            //Actualizar estado
                if (usuario.getActivo() != null) {
                    usuarioDB.setActivo(usuario.getActivo());
                }

                return usuarioRepository.save(usuarioDB);

    }

    // =================================================
// ASIGNAR ROLES A UN USUARIO
// =================================================
// Protecciones:
// - Un usuario no puede modificar sus propios roles.
// - ROLE_ADMIN solo puede ser concedido por un administrador.
// - No se puede retirar ROLE_ADMIN al último administrador activo.
@Override
public Usuario asignarRol(
        Long idUsuario,
        Set<Long> idsRol) {

    // Obtener al usuario autenticado desde Spring Security.
    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    if (authentication == null
            || !(authentication.getPrincipal() instanceof Usuario actor)) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No fue posible identificar al usuario autenticado."
        );
    }

    // Evitar que un usuario modifique sus propios roles.
    if (actor.getIdUsuario() != null
        && actor.getIdUsuario().equals(idUsuario)) {

    AuditoriaService.registrar(
            "ASIGNAR_ROLES",
            "USUARIO_ID_" + idUsuario,
            "FALLO",
            "Intento de modificar los propios roles"
    );

    throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No puedes modificar tus propios roles."
    );
}

    // Buscar al usuario objetivo.
    Usuario usuario =
            usuarioRepository.findById(idUsuario)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "El usuario indicado no existe"
                            )
                    );

    Set<Rol> nuevosRoles =
            new HashSet<>();

    // Buscar cada rol solicitado.
    for (Long idRol : idsRol) {

        Rol rol =
                rolRepository.findById(idRol)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El rol con ID "
                                                + idRol
                                                + " no existe"
                                )
                        );

        nuevosRoles.add(rol);
    }

    boolean asignaRolAdmin =
            nuevosRoles.stream()
                    .anyMatch(rol ->
                            "ROLE_ADMIN".equalsIgnoreCase(
                                    rol.getNombre()
                            )
                    );

    // Para conceder ROLE_ADMIN, el actor también debe
    // tener acceso administrativo.
    if (asignaRolAdmin) {

        boolean actorEsAdministrador =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ACCESO_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );

        if (!actorEsAdministrador) {

    AuditoriaService.registrar(
            "ASIGNAR_ROLES",
            "USUARIO_ID_" + idUsuario,
            "FALLO",
            "Intento no autorizado de asignar ROLE_ADMIN"
    );

    throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No tienes autorización para asignar el rol de administrador."
    );
}
    }

    boolean eraAdministrador =
            usuario.getRoles()
                    .stream()
                    .anyMatch(rol ->
                            "ROLE_ADMIN".equalsIgnoreCase(
                                    rol.getNombre()
                            )
                    );

    boolean seguiraSiendoAdministrador =
            nuevosRoles.stream()
                    .anyMatch(rol ->
                            "ROLE_ADMIN".equalsIgnoreCase(
                                    rol.getNombre()
                            )
                    );

    // Evitar retirar ROLE_ADMIN al último administrador activo.
    if (usuario.isEnabled()
            && eraAdministrador
            && !seguiraSiendoAdministrador) {

        long administradoresActivos =
                usuarioRepository
                        .countByActivoTrueAndRoles_Nombre(
                                "ROLE_ADMIN"
                        );

        if (administradoresActivos <= 1) {

    AuditoriaService.registrar(
            "ASIGNAR_ROLES",
            "USUARIO_ID_" + idUsuario,
            "FALLO",
            "Intento de retirar ROLE_ADMIN al último administrador activo"
    );

    throw new ResourceConflictException(
            "No se puede retirar el rol de administrador al último administrador activo del sistema."
    );
}
    }

    usuario.getRoles().clear();
usuario.getRoles().addAll(nuevosRoles);

Usuario usuarioActualizado =
        usuarioRepository.save(usuario);

AuditoriaService.registrar(
        "ASIGNAR_ROLES",
        "USUARIO_ID_" + idUsuario,
        "EXITO",
        "Roles actualizados correctamente"
);

return usuarioActualizado;
}

    // =================================================
// DESACTIVAR USUARIO
// =================================================
// Antes de desactivar un administrador se valida que
// exista por lo menos otro administrador activo.
@Override
public void desactivar(Long id) {

    Usuario usuario =
            usuarioRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Usuario no encontrado con ID: " + id
                            )
                    );

    boolean esAdministrador =
            usuario.getRoles()
                    .stream()
                    .anyMatch(rol ->
                            "ROLE_ADMIN".equalsIgnoreCase(
                                    rol.getNombre()
                            )
                    );

    // Si el usuario es un administrador activo,
    // no se permite desactivarlo cuando es el último.
    if (usuario.isEnabled() && esAdministrador) {

        long administradoresActivos =
                usuarioRepository
                        .countByActivoTrueAndRoles_Nombre(
                                "ROLE_ADMIN"
                        );

        if (administradoresActivos <= 1) {

    AuditoriaService.registrar(
            "DESACTIVAR_USUARIO",
            "USUARIO_ID_" + id,
            "FALLO",
            "Intento de desactivar al último administrador activo"
    );

    throw new ResourceConflictException(
            "No se puede desactivar al último administrador activo del sistema."
    );
}
    }

    usuario.setActivo(false);

usuarioRepository.save(usuario);

AuditoriaService.registrar(
        "DESACTIVAR_USUARIO",
        "USUARIO_ID_" + id,
        "EXITO",
        "Usuario desactivado correctamente"
);
}

    //Login usuario pacientes
    @Override
    public Usuario registrarPaciente(Usuario usuario){
        usuario.setDebeCambiarPassword(false);

        //Verificar correo dupĺicado
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw  new DuplicateResourceException(
            "Ya existe un usuario registrado con ese correo"
            );
        }

        //  Buscar el Rol del paciente

        Rol rolPaciente = rolRepository.findByNombre("ROLE_PATIENT")
                .orElseThrow(()->
                        new ResourceNotFoundException("El rol ROLE_PATIENT no existe")
                );

        //Cifrar Contraseña

        usuario.setContraseña(passwordEncoder.encode(usuario.getContrasena()));

        //Asignar automaticamente ROLE_PATIENT

        usuario.getRoles().add(rolPaciente);

        //Guardar Usuario

         return usuarioRepository.save(usuario);

    }

}
