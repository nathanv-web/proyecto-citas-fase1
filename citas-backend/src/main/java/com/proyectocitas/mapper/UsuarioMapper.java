package com.proyectocitas.mapper;

import com.proyectocitas.dto.response.UsuarioResponseDTO;
import com.proyectocitas.dto.request.UsuarioCreateRequestDTO;
import com.proyectocitas.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    //Convierte una entidad Usuario a UsuarioResponseDTO
    public UsuarioResponseDTO toDTO(Usuario usuario) {

        //Evitar error si el usuario viene null
        if (usuario == null) {
            return null;
        }

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setCorreo(usuario.getCorreo());
        dto.setTelefono(usuario.getTelefono());
        dto.setActivo(usuario.getActivo());

        //Convertir los objetos Rol solamente a sus nombres
        if (usuario.getRoles() != null) {

            dto.setRoles(
                    usuario.getRoles()
                            .stream()
                            .map(rol -> rol.getNombre())
                            .collect(Collectors.toSet())
            );
        }

        return dto;
    }


    //Convierte un UsuarioCreateRequestDTO a una entidad Usuario
    public Usuario toEntity(UsuarioCreateRequestDTO dto) {

        //Evitar error si el DTO viene null
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setCorreo(dto.getCorreo());
        usuario.setTelefono(dto.getTelefono());

        //La contraseña debe cifrarse en el Service antes de guardarse
        usuario.setContraseña(dto.getContrasena());

        return usuario;
    }
}
