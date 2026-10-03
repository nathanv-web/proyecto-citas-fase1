
package com.proyectocitas.model;

import jakarta.persistence.*;


@Entity
@Table(name = "enfermeros")
public class Enfermero {
    
    // ATRIBUTOS
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id_enfermero")
    private Long idEnfermero;
    
    //RELACIONES
    
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    
    private String colegiado;
    
    private String area;
    
    private String turno;
    
    @Column(name = "codigo_empleado")
    private String codigoEmpleado;
    
    //CONSTRUCTOR
    
    public Enfermero(){
        
    }
    
    //GETTERS Y SETTERS

    public Long getIdEnfermero() {
        return idEnfermero;
    }

    public void setIdEnfermero(Long idEnfermero) {
        this.idEnfermero = idEnfermero;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getColegiado() {
        return colegiado;
    }

    public void setColegiado(String colegiado) {
        this.colegiado = colegiado;
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

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }
    
    
}
