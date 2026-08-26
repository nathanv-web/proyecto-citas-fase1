package com.proyectocitas.service.Impl;

import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.EstadoCita.NombreEstado;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.service.EstadoCitaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoCitaServiceImpl implements EstadoCitaService {

    private final EstadoCitaRepository estadoCitaRepository;

    //Constructor para inyectar el repositorio

    public EstadoCitaServiceImpl(
            EstadoCitaRepository estadoCitaRepository) {

        this.estadoCitaRepository = estadoCitaRepository;
    }

    //Obtener todos los estados de cita

    @Override
    public List<EstadoCita> obtenerTodos() {
        return estadoCitaRepository.findAll();
    }

    //Buscar un estado por su ID

    @Override
    public Optional<EstadoCita> obtenerPorId(Long id) {
        return estadoCitaRepository.findById(id);
    }

    //Buscar un estado por su nombre

    @Override
    public Optional<EstadoCita> obtenerPorNombre(
            NombreEstado nombre) {

        return estadoCitaRepository.findByNombre(nombre);
    }

    //Guardar un nuevo estado de cita

    @Override
    public EstadoCita guardar(EstadoCita estadoCita) {
        return estadoCitaRepository.save(estadoCita);
    }

    //Actualizar solamente los campos que tengan información

    @Override
    public EstadoCita actualizar(
            EstadoCita estadoCita,
            Long id) {

        //Buscar el estado existente
        EstadoCita estadoDB = estadoCitaRepository.findById(id).get();

        //Actualizar nombre si viene con valor
        if (estadoCita.getNombre() != null) {
            estadoDB.setNombre(estadoCita.getNombre());
        }

        //Actualizar descripción si viene con valor
        if (estadoCita.getDescripcion() != null && !estadoCita.getDescripcion().isEmpty()) {

            estadoDB.setDescripcion(
                    estadoCita.getDescripcion()
            );
        }

        //Guardar los cambios realizados
        return estadoCitaRepository.save(estadoDB);
    }

    //Eliminar un estado de cita por su ID

    @Override
    public void eliminar(Long id) {
        estadoCitaRepository.deleteById(id);
    }
}