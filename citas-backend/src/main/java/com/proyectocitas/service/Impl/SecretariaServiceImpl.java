package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Secretaria;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.SecretariaRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.SecretariaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecretariaServiceImpl implements SecretariaService {
    
    //
    
    private final SecretariaRepository secretariaRepository;
    private final UsuarioRepository usuarioRepository;

    //Constructor para inyectar los repositorios

    public SecretariaServiceImpl(
            SecretariaRepository secretariaRepository,
            UsuarioRepository usuarioRepository) {

        this.secretariaRepository = secretariaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Obtener todas las secretarias

    @Override
    public List<Secretaria> obtenerTodos() {
        return secretariaRepository.findAll();
    }

    //Buscar una secretaria por su ID

    @Override
    public Optional<Secretaria> obtenerPorId(Long id) {
        return secretariaRepository.findById(id);
    }

    //Buscar una secretaria por el ID del usuario

    @Override
    public Optional<Secretaria> obtenerPorUsuario(Long idUsuario) {
        return secretariaRepository.findByUsuario_IdUsuario(idUsuario);
    }

    //Guardar una nueva secretaria

    @Override
    public Secretaria guardar(Secretaria secretaria) {
        return secretariaRepository.save(secretaria);
    }

    //Actualizar solamente los campos que tengan información

    @Override
    public Secretaria actualizar(Secretaria secretaria, Long id) {

        //Buscar la secretaria existente
        Secretaria secretariaDB = secretariaRepository.findById(id).get();

        //Actualizar usuario si viene con información
        if (secretaria.getUsuario() != null) {

            Usuario usuarioDB = usuarioRepository.findById(
                    secretaria.getUsuario().getIdUsuario()).get();

            secretariaDB.setUsuario(usuarioDB);
        }

        //Actualizar código de empleado si viene con valor
        if (secretaria.getCodigoEmpleado() != null && !secretaria.getCodigoEmpleado().isEmpty()) {

            secretariaDB.setCodigoEmpleado(
                    secretaria.getCodigoEmpleado()
            );
        }

        //Actualizar área si viene con valor
        if (secretaria.getArea() != null && !secretaria.getArea().isEmpty()) {

            secretariaDB.setArea(secretaria.getArea());
        }

        //Actualizar turno si viene con valor
        if (secretaria.getTurno() != null && !secretaria.getTurno().isEmpty()) {

            secretariaDB.setTurno(secretaria.getTurno());
        }

        //Guardar los cambios realizados
        return secretariaRepository.save(secretariaDB);
    }

    //Eliminar una secretaria por su ID

    @Override
    public void eliminar(Long id) {
        secretariaRepository.deleteById(id);
    }
}