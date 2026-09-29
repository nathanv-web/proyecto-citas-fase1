/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyectocitas.exception;

/*En esta clase de Manejaremos solo las excepciones en el cual no se encuentre el recurso que se esta 
buscando pero no existe

*/

/**
 *
 * @author centinel
 */
public class ResourceNotFoundException  extends RuntimeException{
    
    public ResourceNotFoundException(String mensaje){
        super(mensaje);
    }
    
}
