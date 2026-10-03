package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Enfermero;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.EnfermeroRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.EnfermeroService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnfermeroServiceImpl implements EnfermeroService {

    private final EnfermeroRepository enfermeroRepository;
    private final UsuarioRepository usuarioRepository;

    //Constructor para inyectar los repositorios

    public EnfermeroServiceImpl(
            EnfermeroRepository enfermeroRepository,
            UsuarioRepository usuarioRepository) {

        this.enfermeroRepository = enfermeroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Obtener todos los enfermeros

    @Override
    public List<Enfermero> obtenerTodos() {
        return enfermeroRepository.findAll();
    }

    //Buscar un enfermero por su ID

    @Override
    public Optional<Enfermero> obtenerPorId(Long id) {
        return enfermeroRepository.findById(id);
    }

    //Buscar un enfermero por el ID del usuario

    @Override
    public Optional<Enfermero> obtenerPorUsuario(Long idUsuario) {
        return enfermeroRepository.findByUsuario_IdUsuario(idUsuario);
    }

    //Guardar un nuevo enfermero

    @Override
    public Enfermero guardar(Enfermero enfermero) {
        return enfermeroRepository.save(enfermero);
    }

    //Actualizar solamente los campos que tengan información

    @Override
    public Enfermero actualizar(Enfermero enfermero, Long id) {

        //Buscar el enfermero existente
        Enfermero enfermeroDB = enfermeroRepository.findById(id).get();

        //Actualizar usuario si viene con información
        if (enfermero.getUsuario() != null) {

            Usuario usuarioDB = usuarioRepository.findById(
                    enfermero.getUsuario().getIdUsuario()).get();

            enfermeroDB.setUsuario(usuarioDB);
        }

        //Actualizar colegiado si viene con valor
        if (enfermero.getColegiado() != null && !enfermero.getColegiado().isEmpty()) {

            enfermeroDB.setColegiado(enfermero.getColegiado());
        }

        //Actualizar área si viene con valor
        if (enfermero.getArea() != null && !enfermero.getArea().isEmpty()) {

            enfermeroDB.setArea(enfermero.getArea());
        }

        //Actualizar turno si viene con valor
        if (enfermero.getTurno() != null && !enfermero.getTurno().isEmpty()) {

            enfermeroDB.setTurno(enfermero.getTurno());
        }

        //Actualizar código de empleado si viene con valor
        if (enfermero.getCodigoEmpleado() != null && !enfermero.getCodigoEmpleado().isEmpty()) {

            enfermeroDB.setCodigoEmpleado(
                    enfermero.getCodigoEmpleado()
            );
        }

        //Guardar los cambios realizados
        return enfermeroRepository.save(enfermeroDB);
    }

    //Eliminar un enfermero por su ID

    @Override
    public void eliminar(Long id) {
        enfermeroRepository.deleteById(id);
    }
}