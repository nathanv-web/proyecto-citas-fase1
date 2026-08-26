package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.service.RolService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    //Constructor para inyectar el repositorio

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    //Obtener todos los roles

    @Override
    public List<Rol> obtenerTodos() {
        return rolRepository.findAll();
    }

    //Buscar un rol por su ID

    @Override
    public Optional<Rol> obtenerPorId(Long id) {
        return rolRepository.findById(id);
    }

    //Buscar un rol por su nombre

    @Override
    public Optional<Rol> obtenerPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    //Guardar un nuevo rol

    @Override
    public Rol guardar(Rol rol) {
        return rolRepository.save(rol);
    }

    //Actualizar solamente los campos que tengan información

    @Override
    public Rol actualizar(Rol rol, Long id) {

        //Buscar el rol existente
        Rol rolDB = rolRepository.findById(id).get();

        //Actualizar nombre si viene con valor
        if (rol.getNombre() != null && !rol.getNombre().isEmpty()) {
            rolDB.setNombre(rol.getNombre());
        }

        //Actualizar descripción si viene con valor
        if (rol.getDescripcion() != null && !rol.getDescripcion().isEmpty()) {
            rolDB.setDescripcion(rol.getDescripcion());
        }

        //Guardar los cambios realizados
        return rolRepository.save(rolDB);
    }

    //Eliminar un rol

    @Override
    public void eliminar(Long id) {
        rolRepository.deleteById(id);
    }
}