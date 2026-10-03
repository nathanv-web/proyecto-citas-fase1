package com.proyectocitas.model;

import com.proyectocitas.model.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "secretarias")
public class Secretaria {
    
    //ATRIBUTOS
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_secretaria")
    private Long idSecretaria;
    
    //RELACIONES
    
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "codigo_empleado")
    private String codigoEmpleado;

    private String area;

    private String turno;
    
    // CONSTRUCTOR
    public Secretaria() {
    }
    
    
    // GETTERS Y SETTERS
    
    
    public Long getIdSecretaria() {
        return idSecretaria;
    }

    public void setIdSecretaria(Long idSecretaria) {
        this.idSecretaria = idSecretaria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }
}