package com.proyectocitas.repository;

import com.proyectocitas.model.Usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // =================================================
    // BÚSQUEDA DE USUARIO POR CORREO
    // =================================================
    // Utilizado principalmente durante el proceso de login.
    Optional<Usuario> findByCorreo(String correo);


    // =================================================
    // VALIDAR CORREO DUPLICADO
    // =================================================
    // Permite comprobar que no existan dos usuarios
    // registrados con el mismo correo electrónico.
    boolean existsByCorreo(String correo);


    // =================================================
    // VALIDAR SI UN ROL ESTÁ ASIGNADO
    // =================================================
    // Se utiliza antes de eliminar un rol para comprobar
    // si todavía está asignado a uno o más usuarios.
    boolean existsByRoles_IdRol(Long idRol);

    // =================================================
    // CONTAR ADMINISTRADORES ACTIVOS
    // =================================================
    // Permite verificar cuántos usuarios activos tienen
    // asignado el rol ROLE_ADMIN.
    //
    // Se utilizará para impedir que el sistema quede
    // sin ningún administrador activo.
    long countByActivoTrueAndRoles_Nombre(String nombreRol);

    // =================================================
    // BUSCAR Y FILTRAR USUARIOS
    // =================================================
    // Permite realizar una búsqueda parcial por nombre,
    // apellido o correo, ignorando mayúsculas y minúsculas.
    //
    // Los filtros de estado y rol son opcionales:
    // - search = null: no aplica búsqueda por texto.
    // - activo = null: muestra usuarios activos e inactivos.
    // - rol = null: no filtra por rol.
    //
    // DISTINCT evita usuarios repetidos cuando un usuario
    // tiene más de un rol asignado.
    @Query("""
            SELECT DISTINCT u
            FROM Usuario u
            LEFT JOIN u.roles r
            WHERE (
                :search IS NULL
                OR :search = ''
                OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (
                :activo IS NULL
                OR u.activo = :activo
            )
            AND (
                :rol IS NULL
                OR :rol = ''
                OR LOWER(r.nombre) = LOWER(:rol)
            )
            ORDER BY u.idUsuario
            """)
    List<Usuario> buscarConFiltros(
            @Param("search") String search,
            @Param("activo") Boolean activo,
            @Param("rol") String rol
    );
}
