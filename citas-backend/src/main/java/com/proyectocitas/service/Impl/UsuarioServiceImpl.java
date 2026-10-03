package com.proyectocitas.service.Impl;

import com.proyectocitas.dto.UsuarioUpdateDTO;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import java.util.HashSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public Usuario actualizar(Long id,UsuarioUpdateDTO usuario) {

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
    
    //Asignacion de rol para cualquier usuario y verificacion de excepciones
 @Override
public Usuario asignarRol(
        Long idUsuario,
        Set<Long> idsRol) {

    // Buscar usuario
    Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "El usuario indicado no existe"
                    )
            );


    // Conjunto donde guardaremos los roles encontrados
    Set<Rol> nuevosRoles =
            new HashSet<>();


    // Buscar cada rol enviado
    for (Long idRol : idsRol) {

        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El rol con ID " + idRol + " no existe"
                        )
                );

        nuevosRoles.add(rol);
    }


    // Quitar los roles actuales
    usuario.getRoles().clear();


    // Asignar los nuevos roles
    usuario.getRoles().addAll(nuevosRoles);


    // Guardar cambios
    return usuarioRepository.save(usuario);
}
    
    
    
    //Desactivar Usuarios 
    @Override
    public void desactivar(Long id) {
        
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> 
                new ResourceNotFoundException(
                "El usuario indicado no existe"));
        
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
    //Login usuario pacientes
    @Override 
    public Usuario registrarPaciente(Usuario usuario){
        
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
