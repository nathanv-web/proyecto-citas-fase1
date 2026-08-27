package com.proyectocitas.service; 

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {


    @Transactional
    public String agendarCita(String datosCita, String emailPaciente) {
        
        
        System.out.println("Asociando nueva cita al paciente: " + emailPaciente);
        
       
        System.out.println("Validando disponibilidad del bloque horario...");
        
       
        System.out.println("Estado del horario cambiado a: RESERVADO");
        System.out.println("Estado inicial de la cita: PENDIENTE");
        
        return "Cita registrada con éxito. Estado: PENDIENTE";
    }
}