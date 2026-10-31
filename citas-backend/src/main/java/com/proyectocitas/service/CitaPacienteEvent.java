package com.proyectocitas.service;

import com.proyectocitas.model.Notificacion;

/** Se procesa dentro de la transacción que modifica la cita. */
public record CitaPacienteEvent(Long idCita, Notificacion.Tipo tipo) {
}
