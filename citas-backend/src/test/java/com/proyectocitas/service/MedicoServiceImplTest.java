package com.proyectocitas.service;

import com.proyectocitas.model.Especialidad;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Rol;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.MedicoServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicoServiceImplTest {

    @Test
    void crearMedicoAgregaRolDoctorYConservaLosRolesExistentes() {
        MedicoRepository medicoRepository = mock(MedicoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        EspecialidadRepository especialidadRepository = mock(EspecialidadRepository.class);
        RolRepository rolRepository = mock(RolRepository.class);
        MedicoServiceImpl service = new MedicoServiceImpl(
                medicoRepository, usuarioRepository, especialidadRepository, rolRepository);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7L);
        Rol rolAdmin = new Rol("ROLE_ADMIN", "Administración");
        usuario.setRoles(new HashSet<>());
        usuario.getRoles().add(rolAdmin);
        Rol rolDoctor = new Rol("ROLE_DOCTOR", "Médico");
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(rolRepository.findByNombre("ROLE_DOCTOR")).thenReturn(Optional.of(rolDoctor));

        Medico medico = new Medico();
        Usuario referenciaUsuario = new Usuario();
        referenciaUsuario.setIdUsuario(7L);
        medico.setUsuario(referenciaUsuario);
        when(medicoRepository.saveAndFlush(medico)).thenReturn(medico);

        Medico guardado = service.crearConRolDoctor(medico);

        assertSame(medico, guardado);
        assertSame(usuario, medico.getUsuario());
        assertTrue(usuario.getRoles().contains(rolAdmin));
        assertTrue(usuario.getRoles().contains(rolDoctor));
        verify(usuarioRepository).save(usuario);
        verify(medicoRepository).saveAndFlush(medico);
    }
}
