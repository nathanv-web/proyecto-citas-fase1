package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.model.Usuario;

import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.repository.UsuarioRepository;

import com.proyectocitas.service.MedicoService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;


    //Constructor para inyectar los repositorios

    public MedicoServiceImpl(
            MedicoRepository medicoRepository,
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository) {

        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
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


    //Actualizar solamente los campos que tengan información

    @Override
    public Medico actualizar(Medico medico, Long id) {

        //Buscar el médico existente
        Medico medicoDB =
                medicoRepository.findById(id).get();


        //Actualizar usuario si viene con información
        if (medico.getUsuario() != null) {

            Usuario usuarioDB = usuarioRepository.findById(
                    medico.getUsuario().getIdUsuario()
            ).get();

            medicoDB.setUsuario(usuarioDB);
        }


        //Actualizar especialidad si viene con información
        if (medico.getEspecialidad() != null) {

            Especialidad especialidadDB =
                    especialidadRepository.findById(
                            medico.getEspecialidad()
                                    .getIdEspecialidad()
                    ).get();

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