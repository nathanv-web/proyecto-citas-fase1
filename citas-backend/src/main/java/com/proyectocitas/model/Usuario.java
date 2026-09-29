package com.proyectocitas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails { //Metodo de autenticazion y autorizacion de una persona

    // ATRIBUTOS DE LA TABLA USUARIO
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario") //
    private Long idUsuario; //

    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;

    @Column(unique = true, nullable = false)
    private String correo;
    
    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String contrasena;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
       
    @Column(nullable = false)
    private Boolean activo = true;
    
    // RELACION USUARIO CON ROLES
    //Roles que tiene asignados el usuario
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_rol",
        joinColumns = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
)
private Set<Rol> roles = new HashSet<>();
    //CONSTRUCTOR VACIO
    
    public Usuario(){}

    //FECHAN DE REGISTRO 
    
    @PrePersist
    public void prePersist(){
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
    
    // GETTER Y SETTERS

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContraseña(String contrasena) {
        this.contrasena = contrasena;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles;
    }
    
    // METODOS DE USERDETAILS
    // SPRING SECURITY
    
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {

    Set<GrantedAuthority> authorities =
            new HashSet<>();

    // Recorrer todos los roles del usuario
    for (Rol rol : roles) {

        // =================================================
        // AGREGAR ROL
        // =================================================
        authorities.add(
                new SimpleGrantedAuthority(
                        rol.getNombre()
                )
        );


        // =================================================
        // AGREGAR PERMISOS DEL ROL
        // =================================================
        if (rol.getPermisos() != null) {

            for (Permiso permiso : rol.getPermisos()) {

                authorities.add(
                        new SimpleGrantedAuthority(
                                permiso.name()
                        )
                );
            }
        }
    }

    return authorities;
}
    
    @Override
    public String getUsername(){
        return correo;
    }
    @Override
    public String getPassword(){
        return contrasena;
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }
    @Override 
    public boolean isAccountNonLocked(){
        return true;
    }
    @Override 
    public boolean isCredentialsNonExpired(){
        return true;
    }
    @Override
    public boolean isEnabled() {
       return activo;
}
    
    
}