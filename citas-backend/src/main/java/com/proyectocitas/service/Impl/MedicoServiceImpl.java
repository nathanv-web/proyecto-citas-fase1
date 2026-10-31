package com.proyectocitas.service.Impl;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.model.Rol;

import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.repository.RolRepository;

import com.proyectocitas.service.MedicoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.hibernate.ResourceClosedException;

@Service
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final RolRepository rolRepository;


    //Constructor para inyectar los repositorios

    public MedicoServiceImpl(
            MedicoRepository medicoRepository,
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository,
            RolRepository rolRepository) {

        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
        this.rolRepository = rolRepository;
    }


    //Obtener todos los médicos

    @Override
    public List<Medico> obtenerTodos() {
        return medicoRepository.findAll();
    }
    //Buscar un médico por su ID

    @Override
    public Optional<Medico> obtenerPorId(Long id) {
        return medicoRepository.findById(id);
    }
    //Buscar un médico por el ID del usuario

    @Override
    public Optional<Medico> obtenerPorUsuario(Long idUsuario) {

        return medicoRepository
                .findByUsuario_IdUsuario(idUsuario);
    }
    //Guardar un nuevo médico

    @Override
    public Medico guardar(Medico medico) {
        return medicoRepository.save(medico);
    }

    @Override
    @Transactional
    public Medico crearConRolDoctor(Medico medico) {
        if (medico == null || medico.getUsuario() == null
                || medico.getUsuario().getIdUsuario() == null) {
            throw new IllegalArgumentException("El usuario del médico es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(medico.getUsuario().getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario indicado no existe"));
        Rol rolDoctor = rolRepository.findByNombre("ROLE_DOCTOR")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El rol ROLE_DOCTOR no existe"));

        if (usuario.getRoles() == null) {
            usuario.setRoles(new java.util.HashSet<>());
        }
        usuario.getRoles().add(rolDoctor);
        usuarioRepository.save(usuario);
        medico.setUsuario(usuario);

        // Flush dentro de la transacción para detectar aquí la restricción UNIQUE.
        return medicoRepository.saveAndFlush(medico);
    }
    //Actualizar solamente los campos que tengan información
    @Override
    public Medico actualizar(Medico medico, Long id) {

        //Buscar el médico existente
        Medico medicoDB =
                medicoRepository.findById(id)
                .orElseThrow(()->
                new ResourceClosedException("El medico indicado no existe")
);

        //Actualizar usuario si viene con información
        if (medico.getUsuario() != null) {

            Usuario usuarioDB = usuarioRepository.findById(
                    medico.getUsuario().getIdUsuario()
            )
                    .orElseThrow(()->
                    new  ResourceNotFoundException("El usuario indicado no existe")
                    );

            medicoDB.setUsuario(usuarioDB);
        }


        //Actualizar especialidad si viene con información
        if (medico.getEspecialidad() != null) {

            Especialidad especialidadDB =
                    especialidadRepository.findById(
                            medico.getEspecialidad()
                                    .getIdEspecialidad()
                    )
                    .orElseThrow(()->
                    new ResourceNotFoundException("La especialidad indicada no existe")
                    );

            medicoDB.setEspecialidad(especialidadDB);
        }


        //Actualizar número de colegiado si viene con valor
        if (medico.getColegiado() != null
                && !medico.getColegiado().isEmpty()) {

            medicoDB.setColegiado(
                    medico.getColegiado()
            );
        }


        //Actualizar años de experiencia si viene con valor
        if (medico.getAniosExperiencia() != null) {

            medicoDB.setAniosExperiencia(
                    medico.getAniosExperiencia()
            );
        }


        //Actualizar biografía si viene con valor
        if (medico.getBiografia() != null
                && !medico.getBiografia().isEmpty()) {

            medicoDB.setBiografia(
                    medico.getBiografia()
            );
        }


        //Actualizar estado si viene con valor
        if (medico.getEstado() != null) {

            medicoDB.setEstado(
                    medico.getEstado()
            );
        }


        //Guardar los cambios realizados
        return medicoRepository.save(medicoDB);
    }


    //Eliminar un médico por su ID

    @Override
    public void eliminar(Long id) {

        medicoRepository.deleteById(id);
    }
}
