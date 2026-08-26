package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Especialidad;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.service.EspecialidadService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    //Constructor para el repositorio

    public EspecialidadServiceImpl(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    //Obtener todas las especialidades

    @Override
    public List<Especialidad> obtenerTodos() {
        return especialidadRepository.findAll();
    }

    //Buscar una especialidad por su ID

    @Override
    public Optional<Especialidad> obtenerPorId(Long id) {
        return especialidadRepository.findById(id);
    }

    //Buscar una especialidad por su nombre

    @Override
    public Optional<Especialidad> obtenerPorNombre(String nombre) {
        return especialidadRepository.findByNombre(nombre);
    }

    //Guardar una nueva especialidad

    @Override
    public Especialidad guardar(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }

    //Actualizar solamente los campos que tengan información

    @Override
    public Especialidad actualizar(Especialidad especialidad, Long id) {

        //Buscar la especialidad existente
        Especialidad especialidadDB = especialidadRepository.findById(id).get();

        //Actualizar nombre si viene con valor
        if (especialidad.getNombre() != null && !especialidad.getNombre().isEmpty()) {

            especialidadDB.setNombre(especialidad.getNombre());
        }

        //Actualizar descripción si viene con valor
        if (especialidad.getDescripcion() != null && !especialidad.getDescripcion().isEmpty()) {

            especialidadDB.setDescripcion(especialidad.getDescripcion());
        }

        //Actualizar estado activo si viene con valor
        if (especialidad.getActivo() != null) {
            especialidadDB.setActivo(especialidad.getActivo());
        }

        //Guardar los cambios realizados
        return especialidadRepository.save(especialidadDB);
    }

    //Eliminar una especialidad por su ID

    @Override
    public void eliminar(Long id) {
        especialidadRepository.deleteById(id);
    }
}