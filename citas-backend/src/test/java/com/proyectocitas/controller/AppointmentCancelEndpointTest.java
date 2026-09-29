package com.proyectocitas.controller;

import com.proyectocitas.service.AppointmentService;

import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AppointmentCancelEndpointTest {

    @Test
    void debeExistirEndpointParaCancelarCita() throws Exception {

        AppointmentService appointmentService =
                mock(AppointmentService.class);

        AppointmentController controller =
                new AppointmentController(
                        appointmentService
                );

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();


        mockMvc.perform(
                put("/api/v1/appointments/1/cancel")
                        .principal(
                                () -> "paciente@correo.com"
                        )
        )
        .andExpect(
                status().isOk()
        );
    }
}