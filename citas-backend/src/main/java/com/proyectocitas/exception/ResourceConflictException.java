/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyectocitas.exception;

/**
 *
 * @author centinel
 */
public class ResourceConflictException extends RuntimeException{
    public ResourceConflictException(String message){
        super(message);
    }
}
